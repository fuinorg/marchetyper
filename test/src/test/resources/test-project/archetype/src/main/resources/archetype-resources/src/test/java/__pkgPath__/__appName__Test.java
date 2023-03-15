#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
#set( $delim = '.,_-/' )
#set( $empty = '' )
#set( $StringUtils = $empty.class.forName('org.codehaus.plexus.util.StringUtils') )
package ${pkgName};

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class ExampleAppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {

        assertThat(true).isTrue();

    }

}
