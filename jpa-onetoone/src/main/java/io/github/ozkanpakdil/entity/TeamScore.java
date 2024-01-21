package io.github.ozkanpakdil.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Entity(name = "team_score")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamScore implements Serializable {
    @EmbeddedId
    protected TeamScoreId id;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Goal> goals;
}
