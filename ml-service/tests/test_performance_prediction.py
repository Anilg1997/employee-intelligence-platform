import sys, json
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'src'))
import pandas as pd
import pytest
import performance_model as performance

def test_performance_target_is_excluded_and_splits_are_reproducible():
    data = pd.read_csv(performance.DATA_PATH)
    splits = performance.split_performance_data(data)
    assert 'PerformanceRating' not in performance.PERFORMANCE_FEATURES
    assert [len(s) for s in splits] == [882, 294, 294]
    assert set.union(*(set(s.index) for s in splits)) == set(data.index)
    assert [set(s.index) for s in splits] == [set(s.index) for s in performance.split_performance_data(data)]

def test_training_persists_metrics_and_inference_contract(tmp_path):
    path = tmp_path / 'performance.pkl'
    performance.train_performance_model(path)
    metadata = json.loads(path.with_suffix('.json').read_text())
    assert metadata['target'] == 'PerformanceRating'
    assert metadata['metrics']['test']['accuracy'] >= 0
    row = pd.read_csv(performance.DATA_PATH).iloc[0]
    result = performance.predict_performance({name: row[name] for name in performance.PERFORMANCE_FEATURES})
    assert result['performance_prediction'] in (3, 4)
    assert 0 <= result['performance_probability'] <= 1
    assert result['limitations']

def test_missing_artifact_returns_unavailable_without_training(tmp_path, monkeypatch):
    monkeypatch.setattr(performance, 'MODEL_PATH', tmp_path / 'missing.pkl')
    with pytest.raises(performance.PerformanceModelUnavailable):
        performance.load_performance_model()
