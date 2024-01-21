package io.github.ozkanpakdil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Match {
    @Id
    @GeneratedValue
    protected Integer id;

    @Column(name = "home_team_id")
    private int homeTeamId;

    @Column(name = "away_team_id")
    private int awayTeamId;

    @OneToOne(cascade = CascadeType.ALL)
    private TeamScore homeScore;
    @OneToOne(cascade = CascadeType.ALL)
    private TeamScore awayScore;
}

