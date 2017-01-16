package com.cellexltd.chat.smack;

import org.jivesoftware.smack.packet.Stanza;

/**
 * Created by MAMUN AHMED on 24-Aug-15.
 */
public class CustomTag extends Stanza{

    /**
     * Returns the XML representation of this Element.
     *
     * @return the packet extension as XML.
     */
    @Override
    public CharSequence toXML() {
        return null;
    }

    public CustomTag(String stanzaId, String url) {
        super(stanzaId);
        this.url = url;

    }

    public String url = null;

}
