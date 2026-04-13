package com.daily.dailychineseculture.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 作业列表DTO
 */
public class HomeworkListDTO {
    private List<HomeworkItem> list;
    private int total;

    // 内部类：作业项
    public static class HomeworkItem {
        private Integer homeworkId;
        private String name;
        private Integer isSmallGroupExcellent;
        private Integer isBigGroupExcellent;
        private Date submitTime;
        private String organization;

        /**
         * 证书列表
         */
        private List<CertificateInfo> certificates;

        public HomeworkItem() {}

        public HomeworkItem(Integer homeworkId, String name, Integer isSmallGroupExcellent, Integer isBigGroupExcellent, Date submitTime, String organization) {
            this.homeworkId = homeworkId;
            this.name = name;
            this.isSmallGroupExcellent = isSmallGroupExcellent;
            this.isBigGroupExcellent = isBigGroupExcellent;
            this.submitTime = submitTime;
            this.organization = organization;
            this.certificates = new ArrayList<>();
        }

        public HomeworkItem(Integer homeworkId, String name, Integer isSmallGroupExcellent, Integer isBigGroupExcellent, Date submitTime, String organization, List<CertificateInfo> certificates) {
            this.homeworkId = homeworkId;
            this.name = name;
            this.isSmallGroupExcellent = isSmallGroupExcellent;
            this.isBigGroupExcellent = isBigGroupExcellent;
            this.submitTime = submitTime;
            this.organization = organization;
            this.certificates = certificates != null ? certificates : new ArrayList<>();
        }

        // 证书信息内部类
        public static class CertificateInfo {
            private String type;
            private Date issueTime;

            public CertificateInfo() {}

            public CertificateInfo(String type, Date issueTime) {
                this.type = type;
                this.issueTime = issueTime;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Date getIssueTime() {
                return issueTime;
            }

            public void setIssueTime(Date issueTime) {
                this.issueTime = issueTime;
            }
        }

        public Integer getHomeworkId() {
            return homeworkId;
        }

        public void setHomeworkId(Integer homeworkId) {
            this.homeworkId = homeworkId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getIsSmallGroupExcellent() {
            return isSmallGroupExcellent;
        }

        public void setIsSmallGroupExcellent(Integer isSmallGroupExcellent) {
            this.isSmallGroupExcellent = isSmallGroupExcellent;
        }

        public Integer getIsBigGroupExcellent() {
            return isBigGroupExcellent;
        }

        public void setIsBigGroupExcellent(Integer isBigGroupExcellent) {
            this.isBigGroupExcellent = isBigGroupExcellent;
        }

        public Date getSubmitTime() {
            return submitTime;
        }

        public void setSubmitTime(Date submitTime) {
            this.submitTime = submitTime;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public List<CertificateInfo> getCertificates() {
            return certificates;
        }

        public void setCertificates(List<CertificateInfo> certificates) {
            this.certificates = certificates;
        }
    }

    public List<HomeworkItem> getList() {
        return list;
    }

    public void setList(List<HomeworkItem> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}