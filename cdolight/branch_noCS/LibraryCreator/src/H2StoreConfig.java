


public class H2StoreConfig implements IStoreConfig{
	
	private final String repoName;
	
	public H2StoreConfig(String repoName){
		this.repoName = repoName;
	}

	@Override
	public String getRepositoryName() {
		return repoName;
	}

}
