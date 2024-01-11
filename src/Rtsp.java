import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        //long sessionDuration = System.currentTimeMillis() - sessionStartTime;
        //logger.log(Level.INFO, "Session duration: " + sessionDuration + " milliseconds");

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
    String getDescribe(VideoMetadata meta, int RTP_dest_port) {
        StringWriter rtspHeader = new StringWriter();
        StringWriter rtspBody = new StringWriter();

        // Session description
        // v=  (protocol version)
        rtspBody.write("v=0" + CRLF);
        // o=  (owner/creator and session identifier).
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestampString = dateFormat.format(currentDate);
        rtspBody.write("o=pkoreng " + RTSP_ID + " " + timestampString + " IN IP4 127.0.0.1" + CRLF);
        // s=  (session name)
        rtspBody.write("s=RTSP-Streaming" + CRLF);
        // i = (session information)
        rtspBody.write("i=Eine RTSP-Streaming Session" + CRLF);

        // Time description
        // t= (time the session is active)
        long sessionDuration = System.currentTimeMillis() - sessionStartTime;
        rtspBody.write("t=" + sessionStartTime + " " + sessionDuration + CRLF);

        // Media description
        // m=  (media name and transport address)
        rtspBody.write("m=video 0 RTP/AVP 96"  + CRLF);
        rtspBody.write("a=control:trackID=0" + CRLF);
        rtspBody.write("a=rtpmap:" + MJPEG_TYPE + " JPEG/90000" + CRLF);
        // rtspBody.write("a=mimetype:string;\"video/mjpeg\"" + CRLF);
        rtspBody.write("a=framerate:" + meta.getFramerate() + CRLF);
        // Audio ist not supported yet
        rtspBody.write("m=audio " + "0" + " RTP/AVP " + "0" + CRLF);
        rtspBody.write("a=rtpmap:" + "0" + " PCMU/8000" + CRLF);
        rtspBody.write("a=control:trackID=" + "1" + CRLF);
        //
        rtspBody.write("a=range:npt=0-");
        if (meta.getDuration() > 0.0) {
            rtspBody.write(Double.toString(meta.getDuration()));
        }
        rtspBody.write(CRLF);

        rtspHeader.write("Content-Base: " + VideoFileName + CRLF);
        rtspHeader.write("Content-Type: " + "application/sdp" + CRLF);
        rtspHeader.write("Content-Length: " + rtspBody.toString().length() + CRLF);
        rtspHeader.write(CRLF);

        return rtspHeader + rtspBody.toString();
    }
}