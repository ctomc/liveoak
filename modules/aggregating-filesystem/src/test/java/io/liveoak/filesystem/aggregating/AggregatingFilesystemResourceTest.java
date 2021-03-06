package io.liveoak.filesystem.aggregating;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.filesystem.aggregating.extension.AggregatingFilesystemExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AggregatingFilesystemResourceTest extends AbstractResourceTestCase {

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "aggr-fs", new AggregatingFilesystemExtension() );
        installResource( "aggr-fs", "aggr", JsonNodeFactory.instance.objectNode() );
    }

    @Override
    protected File applicationDirectory() {
        return this.projectRoot;
    }

    @Test
    public void testReadConfig() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), "/admin/applications/testApp/resources/aggr");
        assertThat(result.getProperty("directory")).isEqualTo( new File( applicationDirectory(), "aggr" ).getAbsolutePath() );
    }

}
