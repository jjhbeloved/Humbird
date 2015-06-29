package org.humbird.soa.common.model.common;

import org.humbird.soa.common.codec.Codec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15/3/21.
 */
public class PropsModel implements Serializable {

    private final static String SYMBLE = "=";

    private final static char CSHAP = '#';

    public final static String ENCRPYT = "{code}";

    private int pidx = 0;

    private int cidx = 0;

    private int nidx = 0;

    private boolean flag = false;

    private List<Prop> props = new ArrayList();

    private List<Comments> comms = new ArrayList();

    public PropsModel() {
    }

    public int getPidx() {
        return pidx;
    }

    public int getCidx() {
        return cidx;
    }

    public boolean isFlag() {
        return flag;
    }

    public List<Prop> getProps() {
        return props;
    }

    public List<Comments> getComms() {
        return comms;
    }

    private void addProp(String line) {
        if (line != null && line.trim().length() > 0) {
            line = line.trim();
            int idx = line.indexOf(SYMBLE);
            if (idx > 0 && idx < line.length()) {
                String k = line.substring(0, idx).trim();
                String v = null;
                if (idx == line.length() - 1) {
                    v = "";
                } else {
                    v = line.substring(idx + 1);
                }
                Prop prop = new Prop(k, v);
                props.add(pidx++, prop);
                while (pidx > cidx) {
                    comms.add(cidx++, null);
                }
            }
        }
    }

    private void addComms(String line) {
        if (cidx == pidx) {
            if (line != null && line.length() > 0) {
                Comments comments = new Comments();
                comments.setCols(line.length());
                comments.setRows(1);
                comments.getComments().add(0, line);
                comms.add(cidx++, comments);
            }
        } else {
            int siz = 0;
            if (line != null && (siz = line.length()) > 0) {
                Comments comments = null;
                int alpha = cidx;
                int beta = -1;
                while ((comments = comms.get(--alpha)) == null) {
                    if (alpha <= 0) {
                        return;
                    }
                    if (alpha > 0 && comms.get(alpha - 2) == null) {
                        beta = alpha - 1;

                    }
                }
                if (beta != -1) {
                    props.remove(alpha - 1);
                    cidx--;
                    pidx--;
                }
                if (siz > comments.getCols()) {
                    comments.setCols(siz);
                }
                comments.getComments().add(comments.getRows(), line);
                comments.setRows(comments.getRows() + 1);
            }
        }
    }

    private void addProp(String line, int encrpyt) {
        if (line != null && line.trim().length() > 0) {
            line = line.trim();
            int idx = line.indexOf(SYMBLE);
            if (idx > 0 && idx < line.length()) {
                String k = line.substring(0, idx).trim();
                String v = null;
                if (idx == line.length() - 1) {
                    v = "";
                } else {
                    v = line.substring(idx + 1).trim();
                    if (k.contains(ENCRPYT)) {
                        if(!Codec.isEncrpyt(v)) {   // had encrpyted, don't done
                            try {
                                v = "{" + Codec.ATTRS[encrpyt] + "}" + Codec.enc(encrpyt, v).replace("\n", "");
                            } catch (Exception e) {
                                // ignore
                            }
                            if (!flag) {
                                flag = true;
                            }
                        }
                    }
                }
                Prop prop = new Prop(k, v);
                props.add(pidx++, prop);
                while (pidx > cidx) {
                    comms.add(cidx++, null);
                }
            }
        }
    }

    private void addNull() {
        props.add(pidx++, null);
        comms.add(cidx++, null);
        nidx++;
    }

    public void add(String line) {
        if (line == null || line.length() == 0) {
            addNull();
        } else {
            if (isComment(line)) {
                line = line.substring(1).trim();
                addComms(line);
            } else {
                addProp(line);
            }
        }
    }

    public void add(String line, int encrypt) {
        if (line == null || line.length() == 0) {
            addNull();
        } else {
            if (isComment(line)) {
                line = line.substring(1).trim();
                addComms(line);
            } else {
                addProp(line, encrypt);
            }
        }
    }

    private static boolean isComment(String lines) {
        return lines.charAt(0) == CSHAP;
    }

    public class Comments implements Serializable {

        private int cols = 0;

        private int rows = 0;

        private List<String> comments = new ArrayList();

        public int getCols() {
            return cols;
        }

        public void setCols(int cols) {
            this.cols = cols;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public List<String> getComments() {
            return comments;
        }

    }

    public class Prop implements Serializable {

        private String key;

        private String val;

        public Prop() {
        }

        public Prop(String key, String val) {
            this.key = key;
            this.val = val;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }
    }
}
