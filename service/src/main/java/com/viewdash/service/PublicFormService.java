import com.viewdash.document.Form;
import com.viewdash.service.AbstractService;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PublicFormService extends AbstractService {

    public ResponseEntity<Form> getForm() {
        return ResponseEntity.ok(mongoTemplate.findOne(new Query(Criteria.where("status").is("ACTIVE")), Form.class));
    }

}
