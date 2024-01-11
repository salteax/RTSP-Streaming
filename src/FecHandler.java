import java.util.HashMap;
import java.util.ArrayList;

public class FecHandler extends FecHandlerDemo {
    public FecHandler(int size) {
        super(size);
    }

    public FecHandler(boolean useFec) {
        super(useFec);
    }

    @Override
    boolean checkCorrection(int nr, HashMap<Integer, RTPpacket> mediaPackets) {
        if(fecStack.get(fecNr.get(nr)) == null || fecList.get(nr) == null) {
            return false;
        }

        int c = 0;
        for(Integer i : fecList.get(nr)) {
            if(mediaPackets.get(i) == null) {
                c++;
            }
        }

        return c <= 1;
    }

    @Override
    RTPpacket correctRtp(int nr, HashMap<Integer, RTPpacket> mediaPackets) {
        FECpacket fecPacket = fecStack.get(fecNr.get(nr));

        for(Integer i : fecList.get(nr)) {
            if(i == nr) {
                continue;
            }
            fecPacket.addRtp(mediaPackets.get(i));
        }

        return fecPacket.getLostRtp(nr);
    }
}