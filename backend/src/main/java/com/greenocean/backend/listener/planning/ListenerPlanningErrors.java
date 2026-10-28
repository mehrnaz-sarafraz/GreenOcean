package com.greenocean.backend.listener.planning;
import com.greenocean.backend.listener.live.LiveSupportController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
/** The service transaction has rolled back before this response is produced. */
@RestControllerAdvice(assignableTypes={ListenerPlanningController.class,LiveSupportController.class})
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class ListenerPlanningErrors {
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<Map<String,String>> conflict(){return ResponseEntity.status(409).body(Map.of("code","SUPPORT_CAPACITY_CHANGED","message","Support capacity changed. Refresh the current server state before choosing another action."));}
}
