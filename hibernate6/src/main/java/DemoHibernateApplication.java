import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.internal.SessionImpl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.UUID;

public class DemoHibernateApplication {

    private static SessionFactory sessionFactory;

    private static final String CREATION_TABLE_1 = """
            CREATE TABLE "MY_FIRST_ENTITY" (
                "ID" UUID NOT NULL,
                "TITLE" VARCHAR(100) UNIQUE NOT NULL,
                PRIMARY KEY ("ID")
            );
            """;
    private static final String CREATION_TABLE_2 = """
            CREATE TABLE "MY_SECOND_ENTITY" (
                "ID" UUID NOT NULL,
                "TITLE" VARCHAR(100) NOT NULL,
                PRIMARY KEY ("ID")
            );
            """;
    private static final String CREATION_TABLE_3 = """
            CREATE TABLE "MY_SECOND_ENTITY_MAPPING" (
                "A" UUID NOT NULL,
                "B" UUID NOT NULL,
                PRIMARY KEY ("A", "B"),
                FOREIGN KEY("A") REFERENCES MY_FIRST_ENTITY("ID"),
                FOREIGN KEY("B") REFERENCES MY_SECOND_ENTITY("ID")
            );
            """;

    public static void main(String[] args) {
        if (isH2()) {
            createTables();
        }
        executeQueries();
    }

    private static boolean isH2() {
        Session session = getSessionFactory().openSession();

        try {
            if("H2".equals(((SessionImpl)session).getJdbcConnectionAccess().obtainConnection().getMetaData().getDatabaseProductName()))
                return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // Close the Hibernate session
            session.close();
        }
        return false;

    }

    private static void createTables() {
        try (Session session = getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            session.createNativeMutationQuery(CREATION_TABLE_1)
                    .executeUpdate();
            session.createNativeMutationQuery(CREATION_TABLE_2)
                    .executeUpdate();
            session.createNativeMutationQuery(CREATION_TABLE_3)
                    .executeUpdate();

            transaction.commit();
        }
    }

    private static void executeQueries() {
        try (Session session = getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            session.persist(newMyFirstEntityWithTitle("a"));
            session.flush();
            session.createMutationQuery("UPDATE MY_SECOND_ENTITY SET title=null")
                    .executeUpdate();

            transaction.rollback();
        }

        try (Session session = getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            session.persist(newMyFirstEntityWithTitle("a"));

            transaction.commit();
        }
    }

    private static MyFirstEntity newMyFirstEntityWithTitle(String title) {
        MyFirstEntity myFirstEntity = new MyFirstEntity();
        myFirstEntity.setId(UUID.randomUUID());
        myFirstEntity.setTitle(title);
        return myFirstEntity;
    }

    private static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();

            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(MyFirstEntity.class)
                    .addAnnotatedClass(MySecondEntity.class)
                    .buildMetadata()
                    .buildSessionFactory();
        }
        return sessionFactory;
    }

    @Entity(name = "MY_FIRST_ENTITY")
    public static class MyFirstEntity {

        @Id
        private UUID id;

        @Column(nullable = false, unique = true)
        private String title;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

    }

    @Entity(name = "MY_SECOND_ENTITY")
    public static class MySecondEntity {

        @Id
        private UUID id;

        @Column(nullable = false)
        private String title;

        @OneToOne
        @JoinTable(name = "MY_SECOND_ENTITY_MAPPING",
                joinColumns = {@JoinColumn(referencedColumnName = "id")},
                inverseJoinColumns = {@JoinColumn(referencedColumnName = "id")})
        private MySecondEntity anotherMySecondEntity;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public MySecondEntity getAnotherMySecondEntity() {
            return anotherMySecondEntity;
        }

        public void setAnotherMySecondEntity(MySecondEntity anotherMySecondEntity) {
            this.anotherMySecondEntity = anotherMySecondEntity;
        }

    }

}
