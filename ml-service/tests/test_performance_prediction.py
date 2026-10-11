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

def test_training_persists_metrics_and_inference_contract(tmp_path, monkeypatch):
    path = tmp_path / 'performance.json'
    data = pd.read_csv(performance.DATA_PATH)
    train, validation, test = performance.split_performance_data(data)
    original_fit = performance.Pipeline.fit
    fitted_indices = []

    def guarded_fit(self, X, y, **kwargs):
        fitted_indices.append(set(X.index))
        assert list(X.columns) == performance.PERFORMANCE_FEATURES
        assert set(X.index) == set(train.index)
        return original_fit(self, X, y, **kwargs)

    monkeypatch.setattr(performance.Pipeline, 'fit', guarded_fit)
    model = performance.train_performance_model(path)
    metadata = json.loads(path.read_text(encoding='utf-8'))['metadata']
    artifact = performance.load_artifact(path)
    assert list(tmp_path.iterdir()) == [path]
    assert metadata == artifact['metadata']
    assert fitted_indices == [set(train.index)]
    encoder = model.named_steps['preprocessor'].named_transformers_['categorical']
    for column, categories in zip(performance.CATEGORICAL_FEATURES, encoder.categories_):
        assert set(categories) == set(train[column])
    for name, rows in [('validation', validation), ('test', test)]:
        frame = rows[performance.PERFORMANCE_FEATURES]
        predictions = model.predict(frame)
        assert artifact['model'].predict_proba(frame) == pytest.approx(model.predict_proba(frame))
        assert metadata['metrics'][name]['accuracy'] == pytest.approx(performance.accuracy_score(rows.PerformanceRating, predictions))
        assert metadata['metrics'][name]['balanced_accuracy'] == pytest.approx(performance.balanced_accuracy_score(rows.PerformanceRating, predictions))
        assert metadata['metrics'][name]['f1_macro'] == pytest.approx(performance.f1_score(rows.PerformanceRating, predictions, average='macro'))
    monkeypatch.setattr(performance, 'MODEL_PATH', path)
    assert metadata['target'] == 'PerformanceRating'
    assert metadata['metrics']['test']['accuracy'] >= 0
    row = pd.read_csv(performance.DATA_PATH).iloc[0]
    result = performance.predict_performance({name: row[name] for name in performance.PERFORMANCE_FEATURES})
    assert result['performance_prediction'] in (3, 4)
    assert 0 <= result['performance_probability'] <= 1
    assert result['limitations']

def test_missing_artifact_returns_unavailable_without_training(tmp_path, monkeypatch):
    monkeypatch.setattr(performance, 'MODEL_PATH', tmp_path / 'missing.json')
    with pytest.raises(performance.PerformanceModelUnavailable):
        performance.load_performance_model()
