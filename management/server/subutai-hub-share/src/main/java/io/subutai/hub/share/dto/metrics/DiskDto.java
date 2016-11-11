package io.subutai.hub.share.dto.metrics;


public class DiskDto
{
    private double total = 0.0D;

    private double available = 0.0D;

    private double used = 0.0D;


    public double getTotal()
    {
        return total;
    }


    public void setTotal( final double total )
    {
        this.total = total;
    }


    public double getAvailable()
    {
        return available;
    }


    public void setAvailable( final double available )
    {
        this.available = available;
    }


    public double getUsed()
    {
        return used;
    }


    public void setUsed( final double used )
    {
        this.used = used;
    }
}