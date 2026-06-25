package {{modelPackage}};

{{entityImports}}import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

@Entity
@Table(name = "{{tableName}}")
{{jpaAttributeOverrides}}
@Getter
@Setter
public class {{entity}} extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

{{fieldsDecl}}{{relationsDecl}}
}