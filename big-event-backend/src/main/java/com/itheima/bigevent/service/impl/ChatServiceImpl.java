package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.ChatMessageMapper;
import com.itheima.bigevent.pojo.ChatMessage;
import com.itheima.bigevent.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {

  @Autowired
  private ChatMessageMapper chatMessageMapper;

  // 存储用户对话上下文（内存中，支持多轮对话）
  private final Map<Integer, List<String>> userContexts = new HashMap<>();

  @Override
  public ChatMessage sendMessage(String content, Integer userId, String username) {
    // 保存用户消息
    ChatMessage userMessage = new ChatMessage();
    userMessage.setUserId(userId);
    userMessage.setUsername(username);
    userMessage.setContent(content);
    userMessage.setSender("user");
    chatMessageMapper.add(userMessage);

    // 更新用户上下文
    updateUserContext(userId, content);

    // 生成智能回复（支持上下文和多轮对话）
    String reply = generateIntelligentReply(content, userId);

    // 保存系统回复
    ChatMessage systemMessage = new ChatMessage();
    systemMessage.setUserId(userId);
    systemMessage.setUsername("系统");
    systemMessage.setContent(reply);
    systemMessage.setReply(reply);
    systemMessage.setSender("system");
    chatMessageMapper.add(systemMessage);

    // 更新上下文（包含系统回复）
    updateUserContext(userId, reply);

    return systemMessage;
  }

  /**
   * 更新用户对话上下文
   */
  private void updateUserContext(Integer userId, String message) {
    userContexts.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    // 只保留最近10条消息作为上下文
    List<String> context = userContexts.get(userId);
    if (context.size() > 10) {
      context.remove(0);
    }
  }

  /**
   * 获取用户对话上下文
   */
  private List<String> getUserContext(Integer userId) {
    return userContexts.getOrDefault(userId, new ArrayList<>());
  }

  @Override
  public List<ChatMessage> getHistory(Integer userId, Integer limit) {
    if (limit == null || limit <= 0) {
      limit = 50; // 默认返回50条
    }
    List<ChatMessage> messages = chatMessageMapper.findByUserId(userId, limit);
    // 反转列表，使最新的消息在最后
    java.util.Collections.reverse(messages);
    return messages;
  }

  /**
   * 生成智能回复（支持上下文和多轮对话）
   */
  private String generateIntelligentReply(String userMessage, Integer userId) {
    String message = userMessage.toLowerCase().trim();
    List<String> context = getUserContext(userId);

    // 问候语处理
    if (isGreeting(message)) {
      if (context.isEmpty()) {
        return "您好！欢迎使用BigEvent博客系统！我是您的智能助手，可以帮您解答关于博客使用的问题。\n\n您可以问我：\n• 如何发布文章？\n• 如何使用相册功能？\n• 如何管理分类？\n• 其他使用问题\n\n有什么可以帮助您的吗？";
      } else {
        return "您好！我们又见面了，有什么新问题需要帮助吗？";
      }
    }

    // 检查上下文，理解用户意图
    String lastUserMessage = getLastUserMessage(context);

    // 文章相关
    if (isAboutArticle(message, lastUserMessage)) {
      return handleArticleQuestion(message, lastUserMessage);
    }

    // 登录注册相关
    if (isAboutAuth(message, lastUserMessage)) {
      return handleAuthQuestion(message, lastUserMessage);
    }

    // 相册相关
    if (isAboutAlbum(message, lastUserMessage)) {
      return handleAlbumQuestion(message, lastUserMessage);
    }

    // 帮助相关
    if (isAboutHelp(message, lastUserMessage)) {
      return handleHelpQuestion(message, lastUserMessage);
    }

    // 感谢语
    if (isThanks(message)) {
      return "不客气！如果还有其他问题，随时可以问我。祝您使用愉快！😊";
    }

    // 再见
    if (isGoodbye(message)) {
      return "再见！如果以后有问题，随时欢迎回来咨询。祝您使用愉快！";
    }

    // 无法理解时，提供引导
    return generateFallbackReply(message, context);
  }

  /**
   * 判断是否是问候语
   */
  private boolean isGreeting(String message) {
    return message.matches(".*(你好|hello|hi|hey|早上好|下午好|晚上好|您好).*")
        || message.equals("在吗")
        || message.equals("在")
        || message.length() <= 3 && (message.contains("好") || message.contains("hi"));
  }

  /**
   * 判断是否关于文章
   */
  private boolean isAboutArticle(String message, String lastMessage) {
    return message.contains("文章") || message.contains("article")
        || message.contains("发布") || message.contains("写")
        || message.contains("编辑") || message.contains("删除")
        || (lastMessage != null && lastMessage.contains("文章"));
  }

  /**
   * 判断是否关于登录注册
   */
  private boolean isAboutAuth(String message, String lastMessage) {
    return message.contains("登录") || message.contains("login")
        || message.contains("注册") || message.contains("register")
        || message.contains("账号") || message.contains("密码")
        || message.contains("忘记密码") || message.contains("找回");
  }

  /**
   * 判断是否关于相册
   */
  private boolean isAboutAlbum(String message, String lastMessage) {
    return message.contains("相册") || message.contains("album")
        || message.contains("照片") || message.contains("图片")
        || message.contains("上传") || message.contains("gallery");
  }

  /**
   * 判断是否关于帮助
   */
  private boolean isAboutHelp(String message, String lastMessage) {
    return message.contains("帮助") || message.contains("help")
        || message.contains("怎么") || message.contains("如何")
        || message.contains("教程") || message.contains("使用");
  }

  /**
   * 判断是否是感谢
   */
  private boolean isThanks(String message) {
    return message.contains("谢谢") || message.contains("thank")
        || message.contains("感谢") || message.contains("thanks");
  }

  /**
   * 判断是否是再见
   */
  private boolean isGoodbye(String message) {
    return message.contains("再见") || message.contains("bye")
        || message.contains("拜拜") || message.contains("88");
  }

  /**
   * 处理文章相关问题
   */
  private String handleArticleQuestion(String message, String lastMessage) {
    if (message.contains("发布") || message.contains("写")) {
      return "发布文章的步骤：\n1. 登录后台管理系统\n2. 点击左侧菜单的「文章管理」\n3. 点击「发布文章」按钮\n4. 填写文章标题、内容、选择分类\n5. 上传封面图片（可选）\n6. 选择状态（已发布/草稿）\n7. 点击「发布」按钮\n\n需要我详细说明某个步骤吗？";
    } else if (message.contains("编辑") || message.contains("修改")) {
      return "编辑文章：\n1. 在文章管理页面找到要编辑的文章\n2. 点击文章卡片进入编辑页面\n3. 修改文章内容\n4. 点击「更新」保存修改\n\n您想编辑哪篇文章呢？";
    } else if (message.contains("删除")) {
      return "删除文章：\n1. 在文章管理页面找到要删除的文章\n2. 点击「删除」按钮\n3. 确认删除操作\n\n注意：删除后无法恢复，请谨慎操作。";
    } else if (message.contains("查看") || message.contains("浏览")) {
      return "查看文章：\n• 前台：在首页或文章列表页面浏览所有已发布的文章\n• 后台：登录后在文章管理页面查看所有文章（包括草稿）\n\n您想查看哪类文章？";
    } else {
      return "关于文章功能，我可以帮您解答：\n• 如何发布文章\n• 如何编辑文章\n• 如何删除文章\n• 如何查看文章\n\n您想了解哪个方面？";
    }
  }

  /**
   * 处理登录注册问题
   */
  private String handleAuthQuestion(String message, String lastMessage) {
    if (message.contains("登录") || message.contains("login")) {
      return "登录步骤：\n1. 点击页面右上角的「登录」按钮\n2. 输入您的用户名和密码\n3. 可选择「记住我」（7天免登录）\n4. 点击「登录」按钮\n\n如果忘记密码，可以联系管理员重置。";
    } else if (message.contains("注册") || message.contains("register")) {
      return "注册步骤：\n1. 在登录页面点击「注册」链接\n2. 填写用户名（5-16位字符）\n3. 设置密码（5-16位字符）\n4. 确认密码\n5. 点击「注册」按钮\n\n注册成功后即可登录使用。";
    } else if (message.contains("忘记密码") || message.contains("找回")) {
      return "目前系统暂不支持自助找回密码功能。如果您忘记了密码，可以：\n1. 联系系统管理员重置密码\n2. 或者使用其他账号登录\n\n我们会尽快添加密码找回功能。";
    } else {
      return "关于账号功能，我可以帮您解答：\n• 如何登录\n• 如何注册\n• 忘记密码怎么办\n\n您遇到什么问题了？";
    }
  }

  /**
   * 处理相册问题
   */
  private String handleAlbumQuestion(String message, String lastMessage) {
    if (message.contains("创建") || message.contains("新建")) {
      return "创建相册：\n1. 登录后台管理系统\n2. 点击左侧菜单的「相册管理」\n3. 点击「新建相册」按钮\n4. 填写相册名称和描述\n5. 上传相册封面（可选）\n6. 添加照片到相册\n7. 保存相册\n\n需要我详细说明某个步骤吗？";
    } else if (message.contains("上传") || message.contains("添加")) {
      return "上传照片到相册：\n1. 进入相册管理页面\n2. 选择要添加照片的相册\n3. 点击「添加照片」或「上传」按钮\n4. 选择要上传的图片文件\n5. 可以添加照片描述\n6. 保存即可\n\n支持常见图片格式：JPG、PNG、GIF等。";
    } else if (message.contains("查看") || message.contains("浏览")) {
      return "查看相册：\n• 前台：访问「相册」页面，可以浏览所有公开相册\n• 点击相册卡片可以查看相册内的所有照片\n• 点击照片可以全屏预览\n\n您想查看哪个相册？";
    } else {
      return "关于相册功能，我可以帮您解答：\n• 如何创建相册\n• 如何上传照片\n• 如何查看相册\n• 如何管理相册\n\n您想了解哪个方面？";
    }
  }

  /**
   * 处理帮助问题
   */
  private String handleHelpQuestion(String message, String lastMessage) {
    if (message.contains("功能") || message.contains("什么")) {
      return "BigEvent博客系统主要功能：\n\n📝 **文章管理**\n• 发布、编辑、删除文章\n• 文章分类管理\n• 文章归档查看\n\n📷 **相册功能**\n• 创建相册\n• 上传照片\n• 浏览相册\n\n💬 **留言功能**\n• 发布留言\n• 点赞留言\n\n👤 **用户管理**\n• 用户注册登录\n• 个人信息管理\n• 角色权限管理\n\n您想了解哪个功能的详细使用方法？";
    } else if (message.contains("怎么") || message.contains("如何")) {
      return "我可以帮您解答以下问题：\n• 如何使用文章功能\n• 如何使用相册功能\n• 如何登录注册\n• 如何管理账号\n\n请告诉我您想了解的具体功能，我会详细为您说明。";
    } else {
      return "我是BigEvent博客系统的智能助手，可以帮您解答：\n\n✅ 功能使用问题\n✅ 操作步骤指导\n✅ 常见问题解答\n\n您可以问我：\n• \"如何发布文章？\"\n• \"怎么创建相册？\"\n• \"如何登录？\"\n• \"有哪些功能？\"\n\n请告诉我您的问题，我会尽力帮助您！";
    }
  }

  /**
   * 生成兜底回复（无法理解时）
   */
  private String generateFallbackReply(String message, List<String> context) {
    // 如果上下文中有相关信息，尝试关联回复
    if (!context.isEmpty() && context.size() >= 2) {
      String lastContext = context.get(context.size() - 2);
      if (lastContext.contains("文章")) {
        return "关于文章功能，您可以问我：\n• 如何发布文章？\n• 如何编辑文章？\n• 如何查看文章？\n\n或者您可以尝试重新描述您的问题。";
      } else if (lastContext.contains("相册")) {
        return "关于相册功能，您可以问我：\n• 如何创建相册？\n• 如何上传照片？\n• 如何查看相册？\n\n或者您可以尝试重新描述您的问题。";
      }
    }

    // 提供通用回复和引导
    return "抱歉，我可能没有完全理解您的问题。😅\n\n您可以尝试这样问我：\n• \"如何发布文章？\"\n• \"怎么创建相册？\"\n• \"如何登录？\"\n• \"有哪些功能？\"\n• \"帮助\"\n\n或者您可以更详细地描述一下您遇到的问题，我会尽力帮助您！";
  }

  /**
   * 获取用户最后一条消息
   */
  private String getLastUserMessage(List<String> context) {
    if (context == null || context.isEmpty()) {
      return null;
    }
    // 从后往前找用户消息（跳过系统回复）
    for (int i = context.size() - 1; i >= 0; i--) {
      String msg = context.get(i).toLowerCase();
      // 简单判断：如果消息不是以"您好"、"我可以"等系统回复开头，可能是用户消息
      if (!msg.startsWith("您好") && !msg.startsWith("我可以")
          && !msg.startsWith("关于") && !msg.startsWith("抱歉")) {
        return msg;
      }
    }
    return null;
  }
}
