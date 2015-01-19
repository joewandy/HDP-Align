package mzmatch.ipeak.sort;

public interface SampleHandler<D extends Data, T extends AbstractClustering<D>> {
	public void handleSample(final T clustering);
}
