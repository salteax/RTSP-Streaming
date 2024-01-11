import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Rtsp extends RtspDemo {
    int rtpRcvPort;

    public Rtsp(BufferedReader RTSPBufferedReader, BufferedWriter RTSPBufferedWriter, int rtpRcvPort, String rtspUrl, String videoFileName) {
        super(RTSPBufferedReader, RTSPBufferedWriter, rtpRcvPort, rtspUrl, videoFileName);
        this.rtpRcvPort = rtpRcvPort;
    }

    public Rtsp(BufferedReader RTSPBufferedReader, BufferedWriter RTSPBufferedWriter) {
        super(RTSPBufferedReader, RTSPBufferedWriter);
    }

    @Override
    boolean play() {
        if(state != State.READY) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return false;
        }

        RTSPSeqNb++;
        send_RTSP_request("PLAY");

        // Wait for the response
        logger.log(Level.INFO, "Wait for response...");
        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response");
            return false;
        } else {
            state = State.PLAYING;
            logger.log(Level.INFO, "New RTSP state: PLAYING\n");
            return true;
        }
    }

    @Override
    boolean pause() {
        if(state != State.PLAYING) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return false;
        }

        RTSPSeqNb++;
        send_RTSP_request("PAUSE");

        // Wait for the response
        logger.log(Level.INFO, "Wait for response...");
        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response");
            return false;
        } else {
            state = State.READY;
            logger.log(Level.INFO, "New RTSP state: READY\n");
            return true;
        }
    }

    @Override
    boolean teardown() {
        if(state != State.PLAYING) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return false;
        }

        RTSPSeqNb++;
        send_RTSP_request("TEARDOWN");

        // Wait for the response
        logger.log(Level.INFO, "Wait for response...");

        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response");
            return false;
        } else {
            state = State.INIT;
            logger.log(Level.INFO, "New RTSP state: INIT\n");
            return true;
        }
    }

    @Override
    void describe() {
        if (state != State.INIT) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return;
        }

        RTSPSeqNb++;
        send_RTSP_request("DESCRIBE");

        // Wait for the response
        logger.log(Level.INFO, "Wait for response...");

        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response");
        } /*else {
            state = State.READY;
            logger.log(Level.INFO, "New RTSP state: READY\n");
        }*/
    }

    @Override
    void options() {
        if (state != State.INIT) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return;
        }

        RTSPSeqNb++;
        send_RTSP_request("OPTIONS");

        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response");
        } /*else {
            state = State.READY;
            logger.log(Level.INFO, "New RTSP state: READY\n");
        }*/
    }

    @Override
    void send_RTSP_request(String request_type) {
        try {
            String rtsp = rtspUrl + VideoFileName;

            if(request_type.equals("SETUP")) {
                rtsp = rtsp + "/trackID=0";
            }

            String rtspReq = request_type + " " + rtsp + " RTSP/1.0" + CRLF;
            rtspReq += "CSeq: " + RTSPSeqNb + CRLF;

            if(request_type.equals("SETUP")) {
                rtspReq += "Transport: RTP/AVP;unicast;client_port=" + rtpRcvPort + "-" + rtpRcvPort+1 + CRLF;
            }

            if(!RTSPid.equals("0")) {
                rtspReq += "Session: " + RTSPid + CRLF;
            }

            logger.log(Level.INFO, "C:" + rtspReq);

            RTSPBufferedWriter.write(rtspReq + CRLF);
            RTSPBufferedWriter.flush();

            logger.log(Level.INFO, "*** RTSP-Request " + request_type + " send ***");
        } catch(Exception ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, "Exception caught: " + ex);
            System.exit(0);
        }
        
    }

    @Override
    String getOptions() {

        return "";
    };

    @Override
    String getDescribe(VideoMetadata meta, int RTP_dest_port) {
        System.out.println("Test");
        return "";
    };

}