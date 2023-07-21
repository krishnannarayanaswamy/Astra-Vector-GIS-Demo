package com.datastax.gisdemo.dataLoading;

import com.datastax.astra.sdk.AstraClient;

public interface IDataLoader {
    void loadData(AstraClient astraClient);
}
