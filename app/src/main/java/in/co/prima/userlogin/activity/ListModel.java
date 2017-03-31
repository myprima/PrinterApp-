package in.co.prima.userlogin.activity;

/**
 * Created by Admin on 30-07-2016.
 */
public class ListModel {
    private  String groupname="";
    private  String group_hd="";
    private  String group_desc="";

    /*********** Set Methods ******************/

    public void setgroupname(String groupname)
    {
        this.groupname = groupname;
    }



    public void setgroup_hd(String group_hd)
    {
        this.group_hd = group_hd;
    }

    /*********** Get Methods ****************/

    public String getgroupname()
    {
        return this.groupname;
    }


    public String getGroup_hd()
    {
        return this.group_hd;
    }
}
