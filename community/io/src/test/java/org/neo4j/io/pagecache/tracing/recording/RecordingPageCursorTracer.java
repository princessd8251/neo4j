/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.io.pagecache.tracing.recording;

import java.util.Objects;

import org.neo4j.io.pagecache.PageSwapper;
import org.neo4j.io.pagecache.tracing.EvictionEvent;
import org.neo4j.io.pagecache.tracing.PageCacheTracer;
import org.neo4j.io.pagecache.tracing.PageFaultEvent;
import org.neo4j.io.pagecache.tracing.PinEvent;
import org.neo4j.io.pagecache.tracing.cursor.PageCursorTracer;

/**
 * Recording tracer of page cursor events.
 * Records and counts number of {@link Pin} and {@link Fault} events.
 * Propagate those counters to global page cache tracer during event reporting.
 */
public class RecordingPageCursorTracer extends RecordingTracer implements PageCursorTracer
{

    private int pins = 0;
    private int faults = 0;
    private PageCacheTracer tracer;

    public RecordingPageCursorTracer()
    {
        super( Pin.class, Fault.class );
    }

    @SafeVarargs
    public RecordingPageCursorTracer( Class<? extends Event>... eventTypesToTrace )
    {
        super( eventTypesToTrace );
    }

    @Override
    public long faults()
    {
        return faults;
    }

    @Override
    public long pins()
    {
        return pins;
    }

    @Override
    public long unpins()
    {
        return 0;
    }

    @Override
    public long bytesRead()
    {
        return 0;
    }

    @Override
    public long evictions()
    {
        return 0;
    }

    @Override
    public long evictionExceptions()
    {
        return 0;
    }

    @Override
    public long bytesWritten()
    {
        return 0;
    }

    @Override
    public long flushes()
    {
        return 0;
    }

    @Override
    public PinEvent beginPin( boolean writeLock, final long filePageId, final PageSwapper swapper )
    {
        return new PinEvent()
        {
            @Override
            public void setCachePageId( int cachePageId )
            {
            }

            @Override
            public PageFaultEvent beginPageFault()
            {
                return new PageFaultEvent()
                {
                    @Override
                    public void addBytesRead( long bytes )
                    {
                    }

                    @Override
                    public void done()
                    {
                        pageFaulted( filePageId, swapper );
                    }

                    @Override
                    public void done( Throwable throwable )
                    {
                    }

                    @Override
                    public EvictionEvent beginEviction()
                    {
                        return EvictionEvent.NULL;
                    }

                    @Override
                    public void setCachePageId( int cachePageId )
                    {
                    }
                };
            }

            @Override
            public void done()
            {
                pinned( filePageId, swapper );
            }
        };
    }

    @Override
    public void init( PageCacheTracer tracer )
    {
        this.tracer = tracer;
    }

    @Override
    public void reportEvents()
    {
        Objects.nonNull( tracer );
        tracer.pins( pins );
        tracer.faults( faults );
    }

    private void pageFaulted( long filePageId, PageSwapper swapper )
    {
        faults++;
        record( new Fault( swapper, filePageId ) );
    }

    private void pinned( long filePageId, PageSwapper swapper )
    {
        pins++;
        record( new Pin( swapper, filePageId ) );
    }

    public static class Fault extends Event
    {
        private Fault( PageSwapper io, long pageId )
        {
            super( io, pageId );
        }
    }

    public static class Pin extends Event
    {
        private Pin( PageSwapper io, long pageId )
        {
            super( io, pageId );
        }
    }

}
