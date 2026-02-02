package synth.core;

import synth.cfg.CFG;

import java.util.List;

public interface ISynthesizer {

    public Program synthesize(CFG cfg, List<Example> examples);

}
