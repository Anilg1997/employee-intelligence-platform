package com.employeeintelligence.api.service;
import com.employeeintelligence.api.dto.ModelMetadata; import java.util.List; import java.util.Map; import org.springframework.stereotype.Service;
@Service public class ModelRegistryService {
 private final List<ModelMetadata> models=List.of(new ModelMetadata("attrition","attrition-service-current","existing ML attrition classifier","active",Map.of(),List.of("Probability is a model score, not calibrated certainty.","Synthetic HR data; not a standalone employment decision.")),new ModelMetadata("performance","performance-classifier-v1","random-forest-classifier","active",Map.of(),List.of("Performance scores are not calibrated confidence.","Synthetic HR data.")),new ModelMetadata("promotion-readiness","promotion-readiness-rules-v1","deterministic-business-rules","active",Map.of("ruleCount",4),List.of("Rule score is not a calibrated probability and has no promotion ground truth.","Demo aid requiring human review.")));
 public List<ModelMetadata> list(){return models;}
}
