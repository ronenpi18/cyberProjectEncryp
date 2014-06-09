package project.unitato.encrypchat;


import android.app.IntentService;
import android.content.Intent;

public class MessageService extends IntentService{

    public MessageService() {
        super("EncrypchatMessageReceiver");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
