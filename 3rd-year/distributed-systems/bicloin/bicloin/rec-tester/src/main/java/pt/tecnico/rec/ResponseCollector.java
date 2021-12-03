package pt.tecnico.rec;

import java.util.concurrent.CopyOnWriteArrayList;

public class ResponseCollector<R> extends Thread {

    private volatile Integer numGoodResponses;
    private volatile Integer numBadResponses;
    private CopyOnWriteArrayList<R> responsesList;
    private Integer numRecs;
    private Boolean requireAllRecsToRespond;
    private Integer numMinGoodResponses;

    public ResponseCollector(Integer numRecs, Boolean requireAllRecsToRespond) {
        this.numGoodResponses = 0;
        this.numBadResponses = 0;
        this.responsesList = new CopyOnWriteArrayList<>();
        this.numRecs = numRecs;
        this.numMinGoodResponses = numRecs/2 + 1;
        this.requireAllRecsToRespond = requireAllRecsToRespond;
    }

    public void addResponse(R r) {
        //System.out.printf("[good res]\n");
        //System.out.printf("%s", r);
        responsesList.add(r);
    }

    public void printError(Throwable t) {
        //System.out.printf("[bad res] %s\n", t);
    }

    public void incNumGoodResponses() {
        numGoodResponses++;
    }

    public void incNumBadResponses() {
        numBadResponses++;
    }
    
    public CopyOnWriteArrayList<R> getResponsesList() {
        return responsesList;
    }

	public void run() {
        while(true) {
            //for sys-status
            if(requireAllRecsToRespond && numGoodResponses + numBadResponses == numRecs)
                break;
            //for other requests that only require quorum
            else if(!requireAllRecsToRespond && numGoodResponses >= numMinGoodResponses)
                break;
        }

        synchronized(this) {
            this.notifyAll();
        }
    }

}