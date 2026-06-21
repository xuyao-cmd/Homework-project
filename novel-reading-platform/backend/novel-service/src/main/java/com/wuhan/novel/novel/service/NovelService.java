package com.wuhan.novel.novel.service;

import com.wuhan.novel.novel.client.UserClient;
import com.wuhan.novel.novel.dto.ApiResponse;
import com.wuhan.novel.novel.dto.PublishNovelRequest;
import com.wuhan.novel.novel.dto.UserView;
import com.wuhan.novel.novel.model.Chapter;
import com.wuhan.novel.novel.model.Novel;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NovelService {
    private final Map<Long, Novel> novels = new LinkedHashMap<Long, Novel>();
    private final Map<Long, Novel> pendingNovels = new LinkedHashMap<Long, Novel>();
    private final Map<Long, List<Chapter>> chapterMap = new LinkedHashMap<Long, List<Chapter>>();
    private final Map<Long, String> authorNameCache = new LinkedHashMap<Long, String>();
    private final AtomicLong novelIdGenerator = new AtomicLong(1);
    private final AtomicLong chapterIdGenerator = new AtomicLong(1);
    private final UserClient userClient;

    public NovelService(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostConstruct
    public void init() {
        authorNameCache.put(1L, "平台管理员");
        authorNameCache.put(2L, "青衫烟雨");
        seedAuthorNames();

        Novel first = createSeedNovel("星河彼岸", 101L, "玄幻", "东方玄幻", "https://dummyimage.com/220x300/5b8def/ffffff&text=星河彼岸",
                "少年从边陲小城出发，在星河万族的时代中寻找故乡与真相。",
                Arrays.asList("热血", "升级", "学院"), 920000, 12000, 9.5);
        addChapter(first.getId(), "第一章 星光坠落", "夜色像一张巨大的帷幕，覆盖了青石镇。林澈站在屋顶上，看见一颗蓝色流星划过天际。\n\n第二天清晨，镇外的古井泛起星光，他的命运也从这一刻开始改变。\n\n他知道，自己必须走出这里，去寻找父亲留下的星图。", "2026-05-01");
        addChapter(first.getId(), "第二章 古井秘境", "古井之下并不是水，而是一条通往地下遗迹的阶梯。林澈握紧短剑，小心地踏入黑暗。\n\n墙壁上的纹路像星辰运行的轨道，每走一步，都有细碎的光点在脚下亮起。", "2026-05-03");

        Novel second = createSeedNovel("长安旧梦录", 102L, "历史", "朝堂权谋", "https://dummyimage.com/220x300/d9a24a/ffffff&text=长安旧梦录",
                "一名女史官卷入朝堂疑案，在盛世长安的繁华背后探寻真相。",
                Arrays.asList("悬疑", "权谋", "古风"), 560000, 8300, 9.2);
        addChapter(second.getId(), "第一章 雨夜钟声", "长安城的雨下了一整夜。大理寺门前的铜钟忽然自鸣三声，一封没有署名的案卷被送到沈知微手中。\n\n案卷里只有一句话：真相藏在灯火最亮处。", "2026-05-06");
        addChapter(second.getId(), "第二章 灯市疑云", "上元灯市人潮如织，沈知微在人群中看见了一个本该已经死去的人。\n\n那人戴着银面具，只留下一枚刻着莲花的铜钱。", "2026-05-08");

        Novel third = createSeedNovel("云端代码师", 103L, "都市", "技术创业", "https://dummyimage.com/220x300/6bbf8a/ffffff&text=云端代码师",
                "普通程序员意外获得云端系统，利用技术与智慧解决现实难题。",
                Arrays.asList("技术流", "创业", "轻松"), 310000, 6500, 8.9);
        addChapter(third.getId(), "第一章 深夜告警", "凌晨两点，服务器告警声把陈远从睡梦中惊醒。屏幕上不断跳动的日志，像一场没有硝烟的战斗。\n\n当他修复最后一个异常时，一个陌生的云端控制台自动出现在浏览器里。", "2026-05-12");
        addChapter(third.getId(), "第二章 第一条任务", "控制台上只有一行提示：完成一次稳定发布，奖励架构优化方案。\n\n陈远深吸一口气，决定先从拆分臃肿的单体应用开始。", "2026-05-13");

        addGeneratedNovels(1000);
    }

    private void seedAuthorNames() {
        String[] names = {
                "林照晚", "沈听澜", "顾云舟", "叶知秋", "苏见微", "温折柳", "陆星河", "白鹿眠", "秦疏影", "许青岚",
                "江月白", "宋栖迟", "周南絮", "闻人雪", "谢临川", "洛沉璧", "唐砚初", "纪微雨", "夏安歌", "程望舒",
                "韩清昼", "孟晚棠", "尹松风", "贺兰舟", "姜流萤", "方予安", "盛怀瑾", "黎知夏", "秦暮雪", "岑青禾",
                "傅云深", "林栖梧", "许照眠", "顾怀风", "江听雨", "温如寄", "宋南枝", "叶归鸿", "陆沉舟", "沈青砚",
                "苏云卿", "白砚秋", "周听寒", "纪长歌", "夏扶疏", "唐微澜", "谢停云", "洛清辞", "秦照夜", "许怀瑾",
                "林远黛", "顾南乔", "沈星眠", "江晚照", "陆闻溪", "温青萝", "宋折月", "叶扶光", "苏锦书", "白清欢",
                "纪云起", "唐栖雪", "谢流光", "洛怀沙", "秦一川", "许南烟", "林疏桐", "顾惊鸿", "沈知微", "江泊舟",
                "陆青梧", "温遥夜", "宋听雪", "叶明川", "苏晚晴", "白若水", "纪寒灯", "唐月笙", "谢云归", "洛长风",
                "秦暮川", "许临溪", "林霁月", "顾青蘅", "沈兰舟", "江寻鹤", "陆微明", "温照影", "宋云岫", "叶澄怀",
                "苏扶摇", "白映舟", "纪山月", "唐听竹", "谢南星", "洛闻笛", "秦归雁", "许照川", "林晚词", "顾疏雨"
        };
        for (int i = 0; i < names.length; i++) {
            authorNameCache.put(101L + i, names[i]);
        }
    }

    private Novel createSeedNovel(String title, Long authorId, String category, String subCategory, String cover, String summary,
                                  List<String> tags, int wordCount, int clickCount, double score) {
        Long id = novelIdGenerator.getAndIncrement();
        Novel novel = new Novel(id, title, authorId, category, cover, summary);
        novel.setSubCategory(subCategory);
        novel.setTags(tags);
        novel.setWordCount(wordCount);
        novel.setClickCount(clickCount);
        novel.setScore(score);
        novels.put(id, novel);
        chapterMap.put(id, new ArrayList<Chapter>());
        return novel;
    }

    private void addGeneratedNovels(int targetTotal) {
        String[] titleStarts = {"星海", "长夜", "云城", "浮生", "青山", "银月", "北境", "南风", "归途", "墨影"};
        String[] titleMiddles = {"霜河", "落霞", "孤灯", "远山", "潮声", "星桥", "寒枝", "明镜", "归鸿", "白塔"};
        String[] titleEnds = {"纪事", "秘卷", "旧梦", "行歌", "天书", "问道", "灯火", "风云", "代码", "迷城"};
        String[] categories = {"玄幻", "历史", "都市", "其他"};
        String[][] subCategories = {
                {"东方玄幻", "异世大陆", "修真仙侠"},
                {"架空历史", "朝堂权谋", "古风传奇"},
                {"都市生活", "职场商战", "技术创业"},
                {"悬疑推理", "轻小说", "科幻未来"}
        };
        String[][] tagGroups = {
                {"热血", "成长", "冒险"},
                {"悬疑", "权谋", "古风"},
                {"技术流", "创业", "轻松"},
                {"治愈", "日常", "群像"},
                {"奇遇", "修行", "逆袭"}
        };
        String[] coverColors = {"5b8def", "6bbf8a", "d9a24a", "d87979", "7f8ca3", "8d79c7", "4aa3a2", "b9895a"};

        while (novels.size() < targetTotal) {
            int no = novels.size() + 1;
            String title = titleStarts[no % titleStarts.length]
                    + titleMiddles[(no / titleStarts.length) % titleMiddles.length]
                    + titleEnds[(no / (titleStarts.length * titleMiddles.length)) % titleEnds.length];
            Long authorId = 101L + (no % 100);
            int categoryIndex = no % categories.length;
            String category = categories[categoryIndex];
            String subCategory = subCategories[categoryIndex][(no / categories.length) % subCategories[categoryIndex].length];
            String cover = "https://dummyimage.com/220x300/" + coverColors[no % coverColors.length] + "/ffffff&text=" + title;
            String summary = "一部" + subCategory + "题材原创小说，讲述主角经历挑战、结识伙伴，并一步步完成自己的目标。";
            int wordCount = 120000 + (no * 137) % 880000;
            int clickCount = 1000 + (no * 97) % 50000;
            double score = 8.0 + ((no * 13) % 20) / 10.0;

            Novel novel = createSeedNovel(title, authorId, category, subCategory, cover, summary,
                    Arrays.asList(tagGroups[no % tagGroups.length]), wordCount, clickCount, score);
            addChapter(novel.getId(), "第一章 " + title + "开篇",
                    title + "的故事从一个普通清晨开始。新的线索悄然出现，主角也由此踏上一段未知旅程。",
                    String.format("2026-06-%02d", no % 28 + 1));
        }
    }

    public List<Novel> list(String keyword, String category, String subCategory, Integer limit) {
        List<Novel> result = new ArrayList<Novel>();
        for (Novel novel : novels.values()) {
            boolean keywordMatched = isBlank(keyword) || novel.getTitle().contains(keyword) || novel.getSummary().contains(keyword) || containsTag(novel, keyword);
            boolean categoryMatched = isBlank(category) || "全部".equals(category) || novel.getCategory().equals(category);
            boolean subCategoryMatched = isBlank(subCategory) || "全部".equals(subCategory) || subCategory.equals(novel.getSubCategory());
            if (keywordMatched && categoryMatched && subCategoryMatched) {
                result.add(withAuthorName(novel));
            }
        }
        Collections.sort(result, new Comparator<Novel>() {
            @Override
            public int compare(Novel o1, Novel o2) {
                return o2.getClickCount().compareTo(o1.getClickCount());
            }
        });
        return applyLimit(result, limit);
    }

    public List<Novel> recommend() {
        return list(null, null, null, 20);
    }

    public Novel detail(Long id) {
        Novel novel = novels.get(id);
        if (novel == null) {
            throw new IllegalArgumentException("小说不存在");
        }
        novel.setClickCount(novel.getClickCount() + 1);
        return withAuthorName(novel);
    }

    public List<Chapter> chapters(Long novelId) {
        if (!novels.containsKey(novelId)) {
            throw new IllegalArgumentException("小说不存在");
        }
        List<Chapter> chapters = chapterMap.get(novelId);
        if (chapters == null) {
            throw new IllegalArgumentException("小说不存在");
        }
        return chapters;
    }

    public Chapter read(Long novelId, Long chapterId) {
        List<Chapter> chapters = chapters(novelId);
        for (Chapter chapter : chapters) {
            if (chapter.getId().equals(chapterId)) {
                return chapter;
            }
        }
        throw new IllegalArgumentException("章节不存在");
    }

    public synchronized Novel publish(PublishNovelRequest request, String adminUsername) {
        requireAdmin(adminUsername);
        Novel novel = createNovelFromRequest(request);
        novel.setStatus("连载中");
        novels.put(novel.getId(), novel);
        return withAuthorName(novel);
    }

    public synchronized Novel submitForReview(PublishNovelRequest request) {
        Novel novel = createNovelFromRequest(request);
        novel.setStatus("待审核");
        pendingNovels.put(novel.getId(), novel);
        return withAuthorName(novel);
    }

    public List<Novel> pending(String adminUsername) {
        requireAdmin(adminUsername);
        List<Novel> result = new ArrayList<Novel>();
        for (Novel novel : pendingNovels.values()) {
            result.add(withAuthorName(novel));
        }
        Collections.sort(result, new Comparator<Novel>() {
            @Override
            public int compare(Novel o1, Novel o2) {
                return o2.getId().compareTo(o1.getId());
            }
        });
        return result;
    }

    public synchronized Novel approve(Long id, String adminUsername) {
        requireAdmin(adminUsername);
        Novel novel = pendingNovels.remove(id);
        if (novel == null) {
            throw new IllegalArgumentException("待审核小说不存在");
        }
        novel.setStatus("连载中");
        novels.put(id, novel);
        return withAuthorName(novel);
    }

    private Novel createNovelFromRequest(PublishNovelRequest request) {
        if (request == null || isBlank(request.getTitle()) || isBlank(request.getSummary())) {
            throw new IllegalArgumentException("标题和简介不能为空");
        }
        Long authorId = request.getAuthorId() == null ? 1L : request.getAuthorId();
        String category = isBlank(request.getCategory()) ? "其他" : request.getCategory();
        String subCategory = isBlank(request.getSubCategory()) ? defaultSubCategory(category) : request.getSubCategory();
        String cover = isBlank(request.getCover())
                ? "https://dummyimage.com/220x300/67c23a/ffffff&text=" + request.getTitle().substring(0, 1)
                : request.getCover();
        Long id = novelIdGenerator.getAndIncrement();
        Novel novel = new Novel(id, request.getTitle(), authorId, category, cover, request.getSummary());
        novel.setSubCategory(subCategory);
        novel.setTags(Arrays.asList("新书", "原创"));
        novel.setScore(8.8);
        novel.setClickCount(0);
        novel.setWordCount(0);
        chapterMap.put(id, new ArrayList<Chapter>());
        int wordCount = 0;
        if (request.getChapters() != null && !request.getChapters().isEmpty()) {
            for (PublishNovelRequest.ChapterDraft chapter : request.getChapters()) {
                if (chapter != null && !isBlank(chapter.getTitle()) && !isBlank(chapter.getContent())) {
                    addChapter(id, chapter.getTitle(), chapter.getContent(), LocalDate.now().toString());
                    wordCount += chapter.getContent().length();
                }
            }
        } else {
            String chapterTitle = isBlank(request.getFirstChapterTitle()) ? "第一章 开始" : request.getFirstChapterTitle();
            String chapterContent = isBlank(request.getFirstChapterContent()) ? "这里是第一章内容。" : request.getFirstChapterContent();
            addChapter(id, chapterTitle, chapterContent, LocalDate.now().toString());
            wordCount = chapterContent.length();
        }
        novel.setWordCount(wordCount);
        return novel;
    }

    private void requireAdmin(String username) {
        if (!"admin".equals(username)) {
            throw new IllegalArgumentException("只有平台管理员 admin 可以执行此操作");
        }
    }

    private void addChapter(Long novelId, String title, String content, String updateTime) {
        List<Chapter> chapters = chapterMap.get(novelId);
        Chapter chapter = new Chapter(chapterIdGenerator.getAndIncrement(), novelId, chapters.size() + 1, title, content, updateTime);
        chapters.add(chapter);
        Novel novel = novels.get(novelId);
        if (novel != null && content != null) {
            novel.setWordCount(novel.getWordCount() + content.length());
        }
    }

    private Novel withAuthorName(Novel source) {
        Novel novel = new Novel(source.getId(), source.getTitle(), source.getAuthorId(), source.getCategory(), source.getCover(), source.getSummary());
        novel.setSubCategory(source.getSubCategory());
        novel.setAuthorName(resolveAuthorName(source.getAuthorId()));
        novel.setWordCount(source.getWordCount());
        novel.setClickCount(source.getClickCount());
        novel.setScore(source.getScore());
        novel.setStatus(source.getStatus());
        novel.setTags(source.getTags());
        return novel;
    }

    private String resolveAuthorName(Long authorId) {
        if (authorNameCache.containsKey(authorId)) {
            return authorNameCache.get(authorId);
        }
        try {
            ApiResponse<UserView> response = userClient.getUser(authorId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                String nickname = response.getData().getNickname();
                authorNameCache.put(authorId, nickname);
                return nickname;
            }
        } catch (Exception ignored) {
            // 用户服务临时不可用时，仍然保证小说服务可展示基本数据。
        }
        return "匿名作者";
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    private boolean containsTag(Novel novel, String keyword) {
        if (novel.getTags() == null || isBlank(keyword)) {
            return false;
        }
        for (String tag : novel.getTags()) {
            if (tag != null && tag.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<Novel> applyLimit(List<Novel> source, Integer limit) {
        if (limit == null || limit <= 0 || source.size() <= limit) {
            return source;
        }
        return new ArrayList<Novel>(source.subList(0, limit));
    }

    private String defaultSubCategory(String category) {
        if ("玄幻".equals(category)) {
            return "东方玄幻";
        }
        if ("历史".equals(category)) {
            return "架空历史";
        }
        if ("都市".equals(category)) {
            return "都市生活";
        }
        return "悬疑推理";
    }
}
