package io.github.ozkanpakdil;

import io.github.ozkanpakdil.entity.Goal;
import io.github.ozkanpakdil.entity.Match;
import io.github.ozkanpakdil.entity.TeamScore;
import io.github.ozkanpakdil.entity.TeamScoreId;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.List;

public class Main {
    private static SessionFactory sessionFactory;

    public static void main(String[] args) {
        executeQueries();
    }

    private static void executeQueries() {
        try (Session session = getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            session.persist(Match.builder()
                    .homeTeamId(1)
                    .awayTeamId(2)
                    .awayScore(TeamScore.builder()
                            .id(TeamScoreId.builder()
                                    .matchId(1)
                                    .teamId(1)
                                    .build())
                            .goals(List.of(Goal.builder()
                                    .score(1)
                                    .build()))
                            .build())
                    .homeScore(TeamScore.builder()
                            .id(TeamScoreId.builder()
                                    .matchId(2)
                                    .teamId(2)
                                    .build())
                            .goals(List.of(Goal.builder()
                                    .score(2)
                                    .build()))
                            .build())
                    .build());
            session.flush();

            transaction.commit();
        }

    }

    private static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();

            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(Match.class)
                    .addAnnotatedClass(Goal.class)
                    .addAnnotatedClass(TeamScore.class)
                    .addAnnotatedClass(TeamScoreId.class)
                    .buildMetadata()
                    .buildSessionFactory();
        }
        return sessionFactory;
    }
}