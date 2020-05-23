package sd1920.trab2.impl;

import javax.ws.rs.core.Response;

import sd1920.trab2.Discovery;
import sd1920.trab2.api.Message;
import sd1920.trab2.clients.ClientFactory;
import sd1920.trab2.clients.EmailResponse;
import sd1920.trab2.clients.MessagesEmailClient;

import java.net.URI;
import java.util.concurrent.*;

/**
 * This class contains all the logic of inter-domain communications,
 * including the queue itself, and the thread responsible for executing the requests.
 */
public class Dispatcher {

    private BlockingQueue<Job> jobs;
    private String targetDomain;
    private MessageResource resource;

    private Thread worker;
    private volatile boolean signalStop;

    // *****************  METHODS CALLED FROM OUTSIDE ***********************************
    public Dispatcher(String targetDomain, MessageResource resource) {
        this.targetDomain = targetDomain;
        this.resource = resource;
        this.jobs = new LinkedBlockingQueue<>();
        this.signalStop = false;

        worker = new Thread(this::dispatchLoop);
        worker.start();
    }

    //Adds a new deliverJob to the queue
    public void addDeliverJob(Message message, String target, String sender) {
        jobs.add(new Job.DeliverJob(message, target, sender));
    }

    //Adds a new deleteJob to the queue
    public void addDeleteJob(long mid, String target) {
        jobs.add(new Job.DeleteJob(mid, target));
    }

    public void stop() {
        signalStop = true;
    }

    // *****************  DISPATCHER THREAD METHODS ***********************************
    private void dispatchLoop() {
        MessagesEmailClient client = null;
        while (!signalStop) {
            //Get the next message from the queue, or block if its empty
            Job nextJob = null;
            try {
                nextJob = jobs.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
            if (nextJob == null)
                continue;
            boolean done = false;
            while (!done) {

                //Look for a server
                if (client == null)
                    client = doDiscovery();

                EmailResponse<Void> response;
                //Execute the request
                if (nextJob instanceof Job.DeliverJob) {
                    Job.DeliverJob deliverJob = (Job.DeliverJob) nextJob;
                    Message m = deliverJob.getMessage();
                    response = client.forwardSendMessage(deliverJob.getTarget(), m, resource.getInternalSecret());
                } else {
                    Job.DeleteJob deleteJob = (Job.DeleteJob) nextJob;
                    response = client.forwardDeleteSentMessage(deleteJob.getTarget(),
                            deleteJob.getMid(), resource.getInternalSecret());
                }

                //If the request failed too many times...
                if (response.getStatusCode() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode()) {
                    System.out.println("Failed to contact uri for " + targetDomain);
                    //Will rediscover and try again
                    client = null;
                } else if (response.getStatusCode() == Response.Status.NO_CONTENT.getStatusCode()) {
                    //Request was successful...
                    System.out.println("Job done! " + nextJob);
                    done = true;
                } else {
                    //Request was unsuccessful
                    System.out.println("Unexpect response for job " + nextJob
                            + " in dispatcher for " + targetDomain);
                    //If it was a deliverJob, add an email to the inbox of the sender
                    if (nextJob instanceof Job.DeliverJob) {
                        Job.DeliverJob dJob = (Job.DeliverJob) nextJob;
                        resource.createErrorMessage(dJob.getMessage().getSender(), dJob.getSenderName(),
                                dJob.getMessage().getId(), dJob.getTarget()+"@"+targetDomain);
                        System.out.println("Error email sent to inbox");
                    }
                    done = true;
                }
            }
        }
    }

    //Discovers a server for the remote domain, and creates a client to handle requests to the discovered server
    private MessagesEmailClient doDiscovery() {
        System.out.println("Dispatcher for " + targetDomain + " looking for uris");
        URI[] uris = new URI[0];
        while (uris.length == 0) {
            uris = Discovery.knownUrisOf(targetDomain, 1000, 20);
            if (uris.length == 0)
                System.out.println("Dispatcher for " + targetDomain + " is not finding uris");
        }
        System.out.println("Dispatcher for " + targetDomain + " found " + uris[0]);
        return ClientFactory.getMessagesClient(uris[0], 5, 1000);
    }

    /**
     * This class represents a Job (i.e. a request) to be executed by a dispatcher thread.
     * this job can be either a DeliverJob (i.e. forward a message to another domain) or a DeleteJob
     * (i.e.  forward a deleteMessage to another domain).
     */
    public static abstract class Job {
        String target;

        public Job(String target) {
            this.target = target;
        }

        public String getTarget() {
            return target;
        }


        public static class DeliverJob extends Job {
            private Message message;
            private String senderName;

            public DeliverJob(Message message, String target, String senderName) {
                super(target);
                this.message = message;
                this.senderName = senderName;
            }

            public Message getMessage() {
                return message;
            }

            public String getSenderName() {
                return senderName;
            }

            @Override
            public String toString() {
                return "DeliverJob{" +
                        "target='" + target + '\'' +
                        ", message=" + message +
                        ", senderName='" + senderName + '\'' +
                        '}';
            }
        }

        public static class DeleteJob extends Job {
            private long mid;

            public DeleteJob(long mid, String target) {
                super(target);
                this.mid = mid;
            }

            public long getMid() {
                return mid;
            }

            @Override
            public String toString() {
                return "DeleteJob{" +
                        "target='" + target + '\'' +
                        ", mid=" + mid +
                        '}';
            }
        }
    }
}
