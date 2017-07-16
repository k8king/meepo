package meepo.transform.sink.log;

import meepo.storage.Bit32Store;
import meepo.transform.channel.DataEvent;
import meepo.transform.config.TaskContext;
import meepo.transform.sink.AbstractSink;
import org.roaringbitmap.RoaringBitmap;

/**
 * Created by peiliping on 17-7-13.
 */
public class BitMapLogSink extends AbstractSink {

    private String columnName;

    private int columnPosition;

    private RoaringBitmap bitmap;

    private String keyName;

    public BitMapLogSink(String name, int index, TaskContext context) {
        super(name, index, context);
        this.columnName = context.get("columnName");
        this.columnPosition = context.getInteger("columnPosition", -1);
        this.keyName = context.getString("keyName");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.columnPosition < 0) {
            if (this.columnName == null) {
                this.columnPosition = 0;
            } else {
                super.schema.forEach(x -> {
                    if (columnName.equals(x.getKey())) {
                        columnPosition = super.schema.indexOf(x);
                    }
                });
            }
        }
        this.bitmap = new RoaringBitmap();
    }

    @Override
    public void timeOut() {
    }

    @Override
    public void onEvent(Object event) throws Exception {
        Object x = ((DataEvent) event).getTarget()[this.columnPosition];
        if (x != null) {
            Long i = (Long) x;
            if (i > 0 && i < Integer.MAX_VALUE) {
                this.bitmap.add(i.intValue());
                super.count++;
            }
        }
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        this.bitmap.runOptimize();
        Bit32Store.getInstance().save(this.keyName, this.bitmap);
        this.bitmap = null;
    }
}