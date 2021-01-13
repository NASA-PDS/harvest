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

import gov.nasa.pds.harvest.cfg.model.FiltersCfg;


public class ProductCrawler
{
    private IOFileFilter dirFilter;
    private IOFileFilter fileFilter;
    
    
    public static interface Callback
    {
        public void onFile(File file) throws Exception;
    }
    
    
    public ProductCrawler(FiltersCfg dir)
    {
        if(dir == null) throw new IllegalArgumentException("Directory is null");
        //paths = dir.paths;
        setFileFilter(dir.fileFilterIncludes, dir.fileFilterExcludes);
        setDirectoryFilter(dir.dirFilterExcludes);
    }
    
    
    private void setFileFilter(List<String> includes, List<String> excludes)
    {
        List<IOFileFilter> filters = new ArrayList<IOFileFilter>();        
        filters.add(FileFilterUtils.fileFileFilter());
        
        // Include
        if(includes != null)
        {
            filters.add(new WildcardOSFilter(includes));
        }

        // Exclude
        if(excludes != null)
        {
            filters.add(new NotFileFilter(new WildcardOSFilter(excludes)));
        }
        
        if(includes == null && excludes == null)
        {
            filters.add(new WildcardOSFilter("*.xml"));
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
    
    
    public void crawl(File directory, Callback cb) throws Exception
    {
        Collection<File> files = FileUtils.listFiles(directory, fileFilter, dirFilter);
        
        for(File file: files)
        {
            cb.onFile(file);
        }
    }
}
