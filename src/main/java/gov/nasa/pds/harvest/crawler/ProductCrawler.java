package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.cfg.model.DirectoriesCfg;
import gov.nasa.pds.harvest.util.ExceptionUtils;


public class ProductCrawler
{
    private Logger LOG; 
    
    private List<String> paths;
    private IOFileFilter dirFilter;
    private IOFileFilter fileFilter;
    
    private Callback callback;
    
    
    public static interface Callback
    {
        public boolean onFile(File file);
    }
    
    
    public ProductCrawler(DirectoriesCfg dir, Callback cb)
    {
        LOG = LogManager.getLogger(getClass());
        
        if(dir == null) throw new IllegalArgumentException("Directory is null");
        //paths = dir.paths;
        setFileFilter(dir.fileFilterIncludes, dir.fileFilterExcludes);
        setDirectoryFilter(dir.dirFilterExcludes);

        if(cb == null) throw new IllegalArgumentException("Callback is null");
        this.callback = cb;
    }
    
    
    public void crawl()
    {
        if(paths == null) return;
        
        for(String path: paths)
        {
            File file = new File(path);
            if(!crawl(file))
            {
                break;
            }
        }
    }
    
    
    private void setFileFilter(List<String> includes, List<String> excludes)
    {
        List<IOFileFilter> filters = new ArrayList<IOFileFilter>();        
        filters.add(FileFilterUtils.fileFileFilter());
        
        // Include
        if(includes != null && !includes.isEmpty())
        {
            filters.add(new WildcardOSFilter(includes));
        }

        // Exclude
        if(excludes != null && !excludes.isEmpty())
        {
            filters.add(new NotFileFilter(new WildcardOSFilter(excludes)));
        }
        
        this.fileFilter = new AndFileFilter(filters);
    }


    private void setDirectoryFilter(List<String> excludes)
    {
        List<IOFileFilter> dirFilters = new ArrayList<IOFileFilter>();
        dirFilters.add(FileFilterUtils.directoryFileFilter());

        if(excludes != null && !excludes.isEmpty())
        {
            dirFilters.add(new NotFileFilter(new WildcardOSFilter(excludes)));
        }
    
        this.dirFilter = new AndFileFilter(dirFilters);
    }
    
    
    private boolean crawl(File directory)
    {
        Collection<File> files = FileUtils.listFiles(directory, fileFilter, dirFilter);
        
        for(File file: files)
        {
            try
            {
                if(!callback.onFile(file))
                {
                    return false;
                }
            }
            catch(Exception ex)
            {
                LOG.error(ExceptionUtils.getMessage(ex));
                return false;
            }
        }
        
        return true;
    }
}
