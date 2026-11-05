package com.greenocean.backend.catalog.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
@RestControllerAdvice(assignableTypes={GrowthLibraryController.class,EducationPermissionsController.class,com.greenocean.backend.wellbeing.controller.PrivateWellbeingController.class,com.greenocean.backend.wellbeing.controller.LearningCoursesController.class,com.greenocean.backend.wellbeing.controller.ReviewedQuestionsController.class})
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class GrowthConflictAdvice {
 @ExceptionHandler({org.springframework.dao.DataIntegrityViolationException.class,org.springframework.dao.PessimisticLockingFailureException.class,org.springframework.dao.QueryTimeoutException.class})
 public ResponseEntity<Map<String,String>> changed(){return ResponseEntity.status(409).body(Map.of("code","GROWTH_STATE_CHANGED","message","The protected record changed. Reload its current state before retrying; no partial change was saved."));}
}
