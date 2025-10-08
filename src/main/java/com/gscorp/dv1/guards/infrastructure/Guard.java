package com.gscorp.dv1.guards.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="guards",
  indexes = {
    @Index(name="ix_guards_userid", columnList="user_id")
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Guard {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    /** Id del usuario en tu sistema (tabla users). */
    @Column(name="user_id", nullable=false) Long userId;

    @Column(length=160) String name; // redundante pero útil para reportes rápidos
    @Column(length=40)  String externalId; // legajo / badge opcional

    @Builder.Default
    @Column(nullable=false)
    private Boolean active = true;

}
