package com.hazeltask.executor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.hazeltask.clustertasks.AbstractCallable;
import com.hazeltask.executor.task.HazelcastWork;

public class ShutdownTask extends AbstractCallable<Collection<HazelcastWork>> {
    private static final long serialVersionUID = 1L;

    private boolean isShutdownNow;
    
    protected ShutdownTask(){
        this(null, false);
    }
    
    public ShutdownTask(String topology, boolean isShutdownNow) {
        super(topology);
        this.isShutdownNow = isShutdownNow;
    }

    public Collection<HazelcastWork> call() throws Exception {
        try {
            if(isShutdownNow)
                return this.getDistributedExecutorService().doShutdownNow();
            else
                this.getDistributedExecutorService().doShutdown();
        } catch(IllegalStateException e) {}
        
        return Collections.emptyList();
    }

    @Override
    protected void readChildData(DataInput in) throws IOException {
        isShutdownNow = in.readBoolean();
    }

    @Override
    protected void writChildData(DataOutput out) throws IOException {
        out.writeBoolean(isShutdownNow);
    }

}
