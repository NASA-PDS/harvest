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

import gov.nasa.pds.harvest.cfg.policy.model.Directory;
import gov.nasa.pds.harvest.cfg.policy.model.DirectoryFilter;
import gov.nasa.pds.harvest.cfg.policy.model.FileFilter;


public class ProductCrawler
{
    private List<String> paths;
    private IOFileFilter dirFilter;
    private IOFileFilter fileFilter;
    
    private Callback callback;
    
    
    public static interface Callback
    {
        public void onFile(File file);
    }
    
    
    public ProductCrawler(Directory dir, Callback cb)
    {
        if(dir == null) throw new IllegalArgumentException("Directory is null");
        paths = dir.getPath();
        setFileFilter(dir.getFileFilter());
        setDirectoryFilter(dir.getDirectoryFilter());

        if(cb == null) throw new IllegalArgumentException("Callback is null");
        this.callback = cb;
    }
    
    
    public void crawl()
    {
        if(paths == null) return;
        
        for(String path: paths)
        {
            File file = new File(path);
            crawl(file);
        }
    }
    
    
    private void setFileFilter(FileFilter filter)
    {
        List<IOFileFilter> filters = new ArrayList<IOFileFilter>();        
        filters.add(FileFilterUtils.fileFileFilter());
        
        if(filter != null)
        {
            // Include
            if(filter.getInclude() != null && !filter.getInclude().isEmpty())
            {
                filters.add(new WildcardOSFilter(filter.getInclude()));
            }

            // Exclude
            if(filter.getExclude() != null && !filter.getExclude().isEmpty())
            {
                filters.add(new NotFileFilter(new WildcardOSFilter(filter.getExclude())));
            }
        }
        
        this.fileFilter = new AndFileFilter(filters);
    }


    private void setDirectoryFilter(DirectoryFilter filter)
    {
        if(filter == null || filter.getExclude() == null || filter.getExclude().isEmpty()) return;

        List<IOFileFilter> dirFilters = new ArrayList<IOFileFilter>();
        dirFilters.add(FileFilterUtils.directoryFileFilter());
        dirFilters.add(new NotFileFilter(new WildcardOSFilter(filter.getExclude())));
    
        this.dirFilter = new AndFileFilter(dirFilters);
    }
    
    
    private void crawl(File directory)
    {
        Collection<File> files = FileUtils.listFiles(directory, fileFilter, dirFilter);
        
        for(File file: files)
        {
            try
            {
                callback.onFile(file);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
