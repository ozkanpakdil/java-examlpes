package io.github.ozkanpakdil.redshiftdialect;

import org.hibernate.dialect.PostgreSQLDialect;

import java.sql.Connection;
import java.sql.SQLException;

public class RedshiftDialect extends PostgreSQLDialect {
    /**
     * Creates a RedshiftDialect.
     *
     * @param connection
     *          Connection
     */
    public RedshiftDialect( Connection connection ) throws SQLException {
        super( connection );
    }

    public static final JdbcDialectFactory FACTORY =
            new JdbcDialectFactory( RedshiftDialect.class, DatabaseProduct.POSTGRESQL ) {
                protected boolean acceptsConnection( Connection connection ) {
                    return super.acceptsConnection( connection ) && isDatabase( DatabaseProduct.REDSHIFT, connection );
                }
            };

    public DatabaseProduct getDatabaseProduct() {
        return DatabaseProduct.REDSHIFT;
    }

    @Override
    public String generateInline( List<String> columnNames, List<String> columnTypes, List<String[]> valueList ) {
        return generateInlineGeneric( columnNames, columnTypes, valueList, null, false );
    }

    @Override
    public void quoteStringLiteral( StringBuilder buf, String value ) {
        // '\' to '\\'
        Util.singleQuoteString( value.replaceAll( "\\\\", "\\\\\\\\" ), buf );
    }

    @Override
    public boolean allowsRegularExpressionInWhereClause() {
        return true;
    }

    @Override
    public String generateRegularExpression( String source, String javaRegex ) {
        try {
            Pattern.compile( javaRegex );
        } catch ( PatternSyntaxException e ) {
            // Not a valid Java regex. Too risky to continue.
            return null;
        }

        // We might have to use case-insensitive matching
        javaRegex = DialectUtil.cleanUnicodeAwareCaseFlag( javaRegex );
        StringBuilder mappedFlags = new StringBuilder();
        String[][] mapping = new String[][] { { "i", "i" } };
        javaRegex = extractEmbeddedFlags( javaRegex, mapping, mappedFlags );
        boolean caseSensitive = true;
        if ( mappedFlags.toString().contains( "i" ) ) {
            caseSensitive = false;
        }

        // Now build the string.
        final StringBuilder sb = new StringBuilder();
        // https://docs.aws.amazon.com/redshift/latest/dg/REGEXP_INSTR.html
        sb.append( "REGEXP_INSTR(" );
        sb.append( source );
        sb.append( "," );
        quoteStringLiteral( sb, javaRegex );
        sb.append( ",1,1,0," );
        sb.append( caseSensitive ? "'c'" : "'i'" );
        sb.append( ") > 0" );

        return sb.toString();
    }
}