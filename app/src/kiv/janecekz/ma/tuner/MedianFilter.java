package kiv.janecekz.ma.tuner;

import java.util.LinkedList;
import java.util.ListIterator;

import kiv.janecekz.ma.tuner.Classificator.Result;

public class MedianFilter {
    private class ResultEntry {
        private Result res;
        public int age;
        
        public ResultEntry(Result res) {
            this.res = res;
            this.age = 0;
        }
    }
    
    private final int SIZE = 5;
    
    LinkedList<ResultEntry> values = new LinkedList<ResultEntry>();
    
    public void addValue(Result val) {
        if (values.isEmpty()) {
            values.add(new ResultEntry(val));
            return;
        }

        ListIterator<ResultEntry> it = values.listIterator();
        boolean added = false;
        while (it.hasNext()) {
            if (val.getFreq() > it.next().res.getFreq())
                continue;

            it.previous();
            it.add(new ResultEntry(val));
            added = true;
            break;
        }

        if (!added) {
            values.add(new ResultEntry(val));
        }
        
        for (ResultEntry re : values) {
            re.age++;
        }
        
        ResultEntry entry = null;
        int max = Integer.MIN_VALUE;
        if (values.size() > SIZE) {
            for (ResultEntry re : values) {
                entry = re.age > max ? re : entry;
            }
        }
        
        values.remove(entry);
    }

    public Result getMedian() {
        if (values.isEmpty())
            return null;
        else
            return values.get(values.size() / 2).res;
    }
}
