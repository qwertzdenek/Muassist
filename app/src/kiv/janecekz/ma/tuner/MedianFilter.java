/*
Musicians Assistant
    Copyright (C) 2014  Zdeněk Janeček <jan.zdenek@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package kiv.janecekz.ma.tuner;

import java.util.LinkedList;
import java.util.ListIterator;

import android.util.Log;
import kiv.janecekz.ma.MainActivity;
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
