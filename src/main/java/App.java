

import com.qidianai.bitmaker.event.EvQuote;
import com.qidianai.bitmaker.event.EvTest;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Handler;
import com.qidianai.bitmaker.eventsys.Reactor;

import java.util.logging.Logger;

public class App {
    Logger logger = Logger.getLogger(getClass().getName());

    public void run(){
        logger.info("test");
    }

    public static void main(String[] args) {
        Handler handler = new Handler() {
            @Override
            public void handle(Event ev) {
                System.out.println(ev);
                System.out.println(ev.getData());
            }
        };

        EvQuote q = new EvQuote();
        EvTest t = new EvTest();

        q.setData(t);

        Reactor reactor = new Reactor();


        reactor.start();

        reactor.register(EvQuote.class, handler);
        reactor.register(EvTest.class, handler);


        reactor.publish(q);
        reactor.publish(t);

        WebSocketService service = new WebSocketServiceImpl();

        //WebSocket客户端
        WebSoketClient client = new WebSoketClient(url,service);
        client.start();



        reactor.join();
    }
}
