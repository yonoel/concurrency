package sharedObject;

import java.util.concurrent.ExecutorService;

public class ThisEscape {
    public ThisEscape(ExecutorService service) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(111);
            }
        });
    }
}
