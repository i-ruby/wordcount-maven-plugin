package work.iruby.workcount;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import work.iruby.wordcount.WordCountMojo;

import java.io.File;

/**
 * @author ruby
 * @since 2021/6/5
 */
public class WordCountMojoTests extends AbstractMojoTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWordCountMojo() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/unit/wordcount-plugin-test.xml");
        WordCountMojo mojo = (WordCountMojo) lookupMojo("wordcount", testPom);
        mojo.execute();
    }
}
