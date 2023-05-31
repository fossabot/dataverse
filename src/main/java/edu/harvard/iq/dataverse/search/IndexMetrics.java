package edu.harvard.iq.dataverse.search;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import java.util.logging.Logger;

/**
 * Metrics must be in this bean as Gauges may not be living in a stateless bean.
 * The metric counter would never know which bean instance to use.
 */
@ApplicationScoped
public class IndexMetrics {
    
    private static final Logger logger = Logger.getLogger(IndexMetrics.class.getCanonicalName());
    
    /**
     * Simply initialize this bean on start. Gauges are loaded lazily, thus trigger loading here.
     */
    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object pointless) {
        logger.info("Registring indexing queue gauges now");
    }
    
    @Gauge(unit = MetricUnits.NONE, name = "datasetsCurrentlyIndexing", absolute = true)
    public Integer currentlyIndexing() {
        return IndexServiceBean.INDEXING_NOW.size();
    }
    
    @Gauge(unit = MetricUnits.NONE, name = "datasetsAwaitingIndexing", absolute = true)
    public Integer awaitingIndexing() {
        return IndexServiceBean.NEXT_TO_INDEX.size();
    }
    
}
