public class Rtsp extends RtspDemo {
    public Rtsp(BufferedReader RTSPBufferedReader, BufferedWriter RTSPBufferedWriter, int rtpRcvPort, String rtspUrl, String videoFileName) {
        super(RTSPBufferedReader, RTSPBufferedWriter, rtpRcvPort, rtspUrl, videoFileName);
    }

    @Override
    boolean play() {
        if(state != State.READY) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return false;
        }

        RTSPSeqNb++;
        send_RTSP_request("Play");

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
        // Implement the logic for the PAUSE method
        // Update the RTSP state and send the PAUSE request
        // Handle the server response
        return false; // Modify as needed
    }

    @Override
    boolean teardown() {
        // Implement the logic for the TEARDOWN method
        // Update the RTSP state and send the TEARDOWN request
        // Handle the server response
        return false; // Modify as needed
    }

    @Override
    void describe() {
        // Implement the logic for the DESCRIBE method
        // Send the DESCRIBE request and parse the server response
    }

    @Override
    void options() {
        // Implement the logic for the OPTIONS method
        // Send the OPTIONS request and parse the server response
    }

    @Override
    void send_RTSP_request(String request_type) {
        try {
            String rtsp = rtspUrl + VideoFileName;

            if(request_type.equals("SETUP")) {
                rtsp = rtsp + "/trackID=0";
            }

            String rtspReq = request_type + " " + rtsp + "RTSP/1.0" + CRLF;
            rtspReq += "CSeq:" + RTSPSeqNb + CRLF;

            if(request_type.equals("SETUP")) {
                rtspReq += "Transport: RTP/AVP;unicast;client_port=" + rtpRcvPort + "-" + rtpRcvPort + CRLF;
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

}