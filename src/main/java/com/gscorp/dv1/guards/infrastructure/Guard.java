package com.gscorp.dv1.guards.infrastructure;

import com.gscorp.dv1.sites.infrastructure.Site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="guards",
  indexes = {
    @Index(name="ix_guards_userid", columnList="user_id"),
    @Index(name="ix_guards_site",   columnList="site_id")
  })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Guard {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id del usuario en tu sistema (tabla users). */
    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(length=160)
    private String name; // redundante pero útil para reportes rápidos

    @Column(length=40)
    private String externalId; // legajo / badge opcional

    @Builder.Default
    @Column(nullable=false)
    private Boolean active = true;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="site_id", nullable=false)
    private Site site;

}
