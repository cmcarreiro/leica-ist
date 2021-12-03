package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;

public class RecObserver<R> implements StreamObserver<R> {

    private ResponseCollector collector;

    public RecObserver(ResponseCollector collector) {
        super();
        this.collector = collector;
    }

    @Override
    public void onNext(R r) {
        //System.out.println("Received response: " + r);
        collector.addResponse(r);
        collector.incNumGoodResponses();
    }

    @Override
    public void onError(Throwable throwable) {
        //System.out.println("Received error: " + throwable);
        collector.printError(throwable);
        collector.incNumBadResponses();
    }

    @Override
    public void onCompleted() {
        //System.out.println("Request completed");
    }
}