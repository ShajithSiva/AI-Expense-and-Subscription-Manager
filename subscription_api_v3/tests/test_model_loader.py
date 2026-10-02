"""Offline loader tests with a tiny random model; these do NOT measure V3 accuracy."""
import json
import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
torch = pytest.importorskip('torch')
transformers = pytest.importorskip('transformers')
tokenizers = pytest.importorskip('tokenizers')
from model_service import ModelService
from preprocessing import preprocess_email


@pytest.fixture
def tiny_export(tmp_path):
    from tokenizers import Tokenizer, models, pre_tokenizers, processors
    from transformers import DistilBertConfig, DistilBertForSequenceClassification, PreTrainedTokenizerFast
    torch.manual_seed(42)
    vocab = {word:i for i,word in enumerate(['[PAD]','[UNK]','[CLS]','[SEP]','[MASK]','paid','plan','renew','monthly','.'])}
    backend = Tokenizer(models.WordPiece(vocab=vocab, unk_token='[UNK]'))
    backend.pre_tokenizer = pre_tokenizers.Whitespace()
    backend.post_processor = processors.TemplateProcessing(single='[CLS] $A [SEP]', special_tokens=[('[CLS]',2),('[SEP]',3)])
    tokenizer = PreTrainedTokenizerFast(tokenizer_object=backend, unk_token='[UNK]',pad_token='[PAD]',cls_token='[CLS]',sep_token='[SEP]',mask_token='[MASK]')
    tokenizer.model_input_names = ['input_ids','attention_mask']
    model = DistilBertForSequenceClassification(DistilBertConfig(
        vocab_size=len(vocab), dim=32, hidden_dim=64, n_heads=2, n_layers=1, num_labels=2,
        id2label={0:'Non-Subscription',1:'Subscription'},label2id={'Non-Subscription':0,'Subscription':1}))
    model.eval()
    model.save_pretrained(tmp_path/'transformer')
    tokenizer.save_pretrained(tmp_path/'transformer')
    config={'dataset_version':'v3','subscription_label_id':1,'vendor_masking':False,
            'text_weight':1.0,'recurrence_weight':0.0,'binary_threshold':0.57,'max_length':256}
    (tmp_path/'configuration.json').write_text(json.dumps(config))
    (tmp_path/'preprocessing_config.json').write_text(json.dumps({'subscription_label_id':1,'vendor_masking':False,'max_length':256}))
    return tmp_path,model,tokenizer


def test_loader_matches_direct_inference_and_reads_saved_threshold(tiny_export):
    folder,model,tokenizer=tiny_export
    service=ModelService(folder)
    text='<p>paid plan renew monthly.</p> https://example.com'
    with torch.inference_mode():
        expected=model(**tokenizer(preprocess_email(text),return_tensors='pt',padding=True,truncation=True,max_length=256)).logits.softmax(-1)[0,1].item()
    result=service.score(text)
    assert result.probability==pytest.approx(expected,abs=1e-6)
    assert result.truncated is False
    assert service.threshold==.57


def test_actual_tokenizer_reports_truncation(tiny_export):
    folder,_,_=tiny_export
    result=ModelService(folder).score('paid '*300)
    assert result.truncated is True and 0<=result.probability<=1
