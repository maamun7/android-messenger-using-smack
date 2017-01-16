package com.cellexltd.chat.smack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Handler;


public class MainActivity extends Activity {

    final public String HOST = "192.168.1.191";
    final public String RESOURCE = "Smack";
    final public String SERVER_NAME = "cellex-chat-server";
    final public int PORT = 5222;

    private EditText mRecipient;
    private EditText mSendText;
    private EditText loginID;
    //private ListView mList;
    private Button btnMsgSend;
    private Button btnLogin;
    private Button btnImgSend;
   // private Button btnFrndList;
    private ImageView pictureView;
    AbstractXMPPConnection connection;
    Context contex;
    private android.os.Handler aHandler;

    private int PICK_IMAGE_REQUEST = 1;

    private File aFile;


    // private CharSequence userID = "test";
    private String password = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecipient = (EditText) findViewById(R.id.eTRecipent);
        mSendText = (EditText) findViewById(R.id.etMSGText);
        loginID = (EditText) findViewById(R.id.etLoginId);
        //mList = (ListView) findViewById(R.id.lVMsgList);
        btnMsgSend = (Button) findViewById(R.id.btnSend);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnImgSend = (Button) findViewById(R.id.btnSendImage);
        //btnFrndList = (Button) findViewById(R.id.btnGetBuddyList);
        pictureView = (ImageView) findViewById(R.id.imgView);
       // pictureView.setVisibility(ImageView.GONE);

        // Create the configuration for this new connection
        final XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        // configBuilder.setUsernameAndPassword("test@cellex-chat-server/Smack", "123456");
        configBuilder.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled);
        configBuilder.setResource(RESOURCE);
        configBuilder.setServiceName(HOST);
        configBuilder.setHost(HOST);
        configBuilder.setPort(PORT);
        connection = new XMPPTCPConnection(configBuilder.build());

        aHandler = new android.os.Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(aFile.getAbsolutePath(), bmOptions);
                bitmap = Bitmap.createScaledBitmap(bitmap, 100,100, true);
                pictureView.setImageBitmap(bitmap);
            }
        };

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String userID = loginID.getText().toString();

                Log.i("STAAAAAAAAA-11111", "BUTTONNNNNN CLICKEDDDDDDDDD");


                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {

                        Log.d("STAAAAAAAA-22222", "ISIDE ASYSNCTASK");

                        //For connection to the server

                        try {
                            connection.connect();
                        } catch (XMPPException e) {
                            e.printStackTrace();
                            Log.e("CONNECTIONS STATUS 11", "CONNECTION error " + e.toString());
                        } catch (SmackException e) {
                            e.printStackTrace();
                            Log.e("CONNECTIONS STATUS 22", "CONNECTION error " + e.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("CONNECTIONS STATUS 33", "CONNECTION error " + e.toString());
                        }

                        if (!connection.isConnected()) {
                            Log.d("FINAL CONNECTION STATUS", "FAILED");
                        } else {
                            Log.d("FINAL CONNECTION STATUS", "YES OKAY");

                            //call receive file listener
                            receiveFile(connection);
                        }

                        //For login with the sever
                        try {
                            //Need the following two line to loging using smack 4.1
                            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
                            SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
                            connection.login(userID, password);
                            Log.d("LOGIN STATUS", "CONNNECCCCTTTEDDDDDDDDDDDDDDDDDDDEEDDDD");
                        } catch (XMPPException e) {
                            e.printStackTrace();
                            Log.d("LOGIN ERROR-111", "Login error " + e.toString() + "USER:" + userID + "PASS:" + password);
                        } catch (SmackException e) {
                            e.printStackTrace();
                            Log.d("LOGIN ERROR-222", "Login error " + e.toString() + "USER:" + userID + "PASS:" + password);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("LOGIN ERROR-333", "Login error " + e.toString() + "USER:" + userID + "PASS:" + password);
                        }

                        return null;
                    }
                }.execute();
            }
        });

        //To Sending the message
        btnMsgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("STAAAAAAAAA", "SEND MESSAFGE BUTTONNNNNN CLICKEDDDDDDDDD");

                String to = mRecipient.getText().toString() + "@cellex-chat-server";
                String text = mSendText.getText().toString();

                String senderId = loginID.getText().toString() + "@cellex-chat-server";

                //Send message
                ChatManager chatmanager = ChatManager.getInstanceFor(connection);
                chatmanager.addChatListener(new ChatManagerListener() {
                    @Override
                    public void chatCreated(Chat chat, boolean createdLocally) {

                    }
                });

                Chat newChat = chatmanager.createChat(to);
                try {
                    newChat.sendMessage(text);
                    Log.i("STAAAAAAAAATus", "MESSAGE SEND");
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                    Log.d("MESSAGE SEND STSTSS", "EEEEEEERRRRRROOOOOOORRR" + e.toString());
                }


                //Send group message
                /*MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);

                // Create a MultiUserChat using an XMPPConnection for a room
                MultiUserChat muc2 = manager.getMultiUserChat("Cellex@conference.cellex-chat-server");
                Log.i("ROOOOOMM INFO","IS : "+muc2.getRoom());

                //Create room
               *//* try {
                    muc2.create("HelloRoom");
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException e) {
                    e.printStackTrace();
                }*//*

                muc2.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Message message) {

                    }
                });

                // Send an empty room configuration form which indicates that we want
                // an instant room
                try {
                    muc2.sendConfigurationForm(new Form(DataForm.Type.submit));
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                // User2 joins the new room
                // The room service will decide the amount of history to send

                try {
                    muc2.join("CellexUser");
                    Log.i("JOIN GROUP CHAT------:", "SUCCESS");
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                    Log.i("JOIN GROUP CHAT", " ERROR 1 : " + e);
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                    Log.i("JOIN GROUP CHAT", " ERROR 2 : " + e);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                    Log.i("JOIN GROUP CHAT", " ERROR 3 : " + e);
                }
                //
                if (muc2.isJoined()) {
                    Log.i("ROOM NAME:", "is: :" + muc2.getRoom());
                }else {
                    Log.i("CANT NOT JOIN", "WITH ROOM");
                }

                //Send requist
                try {
                    muc2.invite("aaaa@cellex-chat-server/Smack", "Meet me in this excellent room");
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                //Received messages

                //Send messages
                Message aMSG = muc2.createMessage();
                aMSG.setBody("HELLLO GROUP");
                try {
                    muc2.sendMessage(text);
                    Log.i("SEND GROUP MESSAGE:", "SUCCESS");
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                    Log.i("SEND GROUP MESSAGE:", "ERROR :"+e);
                }*/
            }
        });

        //To sendign the imamge
        btnImgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(
                    selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);



            //File path
           /* String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            final String fileName = "test.jpg";
            final String path_file = baseDir + "/Smack/" + fileName;*/

            File sendFile = new File(filePath);



            final String senderId = loginID.getText().toString();

            Log.i("STAAAAAAAAA", "SEND IMAGE BUTTONNNNNN CLICKEDDDDDDDDD");
            ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);

            FileTransferNegotiator transferNegoTiate = FileTransferNegotiator.getInstanceFor(connection);
            FileTransferNegotiator.IBB_ONLY = true; //New added
            FileTransferNegotiator.getInstanceFor(connection);

            if (FileTransferNegotiator.isServiceEnabled(connection)) {
                Log.i("SERVICE IS ", "ENABLE ");
            } else {
                Log.i("SERVICE IS ", "Not ENABLE ");
            }



            if (!sendFile.exists()) {
                Log.i("BBAAASEEE PAAATHHH", "FILEEEEEEE ISSSNOOOT EXISSST");
            } else {
                Log.i("BBAAASEEE PAAATHHH", filePath);
            }


            //Get receipent id
            String to = mRecipient.getText().toString()+"@cellex-chat-server/Smack";
            Log.i("Sending ", "--" + filePath + " to " + to);
            final FileTransferManager manager = FileTransferManager.getInstanceFor(connection);
            final OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(to);
            final File jarFile = new File(filePath);


            // Send the file
            try {
                transfer.sendFile(new File(filePath), "You won't believe this!");
                Log.d("FILE TRANSFFER", "STATUS" + transfer.getStatus());
            } catch (SmackException e) {
                e.printStackTrace();
            }

            new AsyncTask<Void, Void, Void>() {
                protected void onPreExecute() {

                }

                @Override
                protected Void doInBackground(Void... params) {


                    while (!transfer.isDone()) {
                        if (transfer.getStatus().equals("Error")) {
                            Log.d("file transfer", "ERROR!!! " + transfer.getError());
                        } else if (transfer.getStatus().equals("Cancelled") || transfer.getStatus().equals("Refused")) {
                            Log.d("file transfer---ERRROR", "Cancelled!!! " + transfer.getError());
                        }

                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    return null;
                };

                protected void onPostExecute(Void result) {

                    if (transfer.getStatus().equals("Error")) {
                        Log.i("file transfer", "refused cancelled error " + transfer.getError());
                    } else {
                        Log.i("file transfer", "SSSSSSSTTTTTAAAATTTTUUUUUUSSSSSSSSSSS: " + transfer.getStatus());
                        Log.i("GET FILE NAME ", "NAME is : " + transfer.getFileName());
                        Log.i("GET FILE SIZEEEEE", "------" + transfer.getFileSize());
                        Log.i("GET FILE PATTHHH", "------" + transfer.getFilePath());
                        Log.i("GET FILE Exceptionss", "------" + transfer.getException());
                        Log.i("GET FILE Error", "------" + transfer.getError());
                    }
                };
            }.execute();
        }
    }

    public void receiveFile(final AbstractXMPPConnection connection) {

        Log.i("RECEIVE:", "File receiving Listner Called");

        Thread thread = new Thread() {

            public void run() {

                final FileTransferManager manager = FileTransferManager.getInstanceFor(connection);
                // Create the listener
                manager.addFileTransferListener(new FileTransferListener() {
                    public void fileTransferRequest(FileTransferRequest request) {


                        final String requestorId = request.getRequestor();
                        Log.i("FileTransferRequest:", "IDDDD " + requestorId);

                        //if (requestorId.contains(senderId)) {
                        final IncomingFileTransfer transfer = request.accept();
                        Log.i("FILETRANSFER REQUEST", "ACCEPPTEED");

                        //File Receiving Path
                        String receiveDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                        final String receiving_path = receiveDir + "/Smack";


                        try {


                            final String fileName = transfer.getFileName();
                            transfer.recieveFile(new File(receiving_path +"/"+ fileName));
                            Log.i("TRANSFER FILE NAME: ", transfer.getFileName());
                            Log.i("REQUEST FILE NAME: ", request.getFileName());
                            Log.i("REQUEST DESCRIPTION: ", request.getDescription());
                            Log.i("TRANSFER STATUS IS", "STATUS: " + transfer.getStatus());

                            //transfer.recieveFile(new File(request.getFileName()));
                            Log.e("SAVING FILE ", "SAVING FIE RECIVED FROM XMPP");

                            while (!transfer.isDone()) {

                                final double progress = transfer.getProgress();
                                final double progressPercent = progress * 100.0;
                                String percComplete = String.format("%1$,.2f", progressPercent);
                                Log.i("TRANSFER PROGRESS", "STATUS 1 IS: " + transfer.getStatus());
                                Log.i("TRANSFER PROGRESS", "STATUS 2 IS: " + percComplete + "% complete");
                                Thread.sleep(3000);
                            }

                            Log.i("FileTransfer complete", "YESS");


                        } catch (SmackException e) {
                            Log.i("IOEXCEPTION TRYING TO-1", "RECEIVE JAR FILE " + e);
                        } catch (InterruptedException e) {
                            //Do nothing
                        } catch (IOException e) {
                            Log.i("IOEXCEPTION TRYING TO-2", "RECEIVE JAR FILE " + e);
                        }
                       /* }else{
                            Log.i("FILETRANSFER REQUEST", "REJECTED");
                            try{
                                request.reject();
                            }catch (SmackException.NotConnectedException e){
                                Log.i("FILETRANSFER REQUEST", "NOTCONNECTEDEXCEPTION WHEN REJECTING FILETRANSFERREQUEST");

                            }
                        }*/

                        String pathName = receiving_path +"/"+ request.getFileName();
                        System.out.println("FILE SEND BY:  " + request.getRequestor());
                        String from = request.getRequestor().substring(0, 26);
                        System.out.println("FILE SEND BY: " + from);
                        String action = request.getDescription();
                        Log.d("File DESCRIPTION", action);

                        pictureView.setVisibility(ImageView.VISIBLE);
                        //pictureView.setImageBitmap();

                         aFile = new File(pathName);

                        aHandler.sendEmptyMessage(0);


                    }
                });

            }
        };
        thread.start();

    }
}

