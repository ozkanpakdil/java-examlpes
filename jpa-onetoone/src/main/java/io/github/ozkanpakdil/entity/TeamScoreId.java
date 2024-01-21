package io.github.ozkanpakdil.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamScoreId implements Serializable {
    @Column(name = "match_id")
    private Integer matchId;
    @Column(name = "team_id")
    private int teamId;
}
