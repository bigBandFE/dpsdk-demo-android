# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.dragonpass.en.sparkapp.entity.** { *; }

# Keep SSO crypto classes (RSA key parsing via JCA — must not be obfuscated)
-keep class com.dragonpass.en.dpsdk.demo.sso.** { *; }

-dontwarn com.chenlb.mmseg4j.ComplexSeg
-dontwarn com.chenlb.mmseg4j.Dictionary
-dontwarn com.chenlb.mmseg4j.MMSeg
-dontwarn com.chenlb.mmseg4j.Seg
-dontwarn com.github.promeg.pinyinhelper.Pinyin$Config
-dontwarn com.github.promeg.pinyinhelper.Pinyin
-dontwarn com.github.stuxuhai.jpinyin.PinyinFormat
-dontwarn com.google.common.collect.ArrayListMultimap
-dontwarn com.google.common.collect.Multimap
-dontwarn com.hankcs.hanlp.HanLP
-dontwarn com.hankcs.hanlp.seg.Segment
-dontwarn com.huaban.analysis.jieba.JiebaSegmenter$SegMode
-dontwarn com.huaban.analysis.jieba.JiebaSegmenter
-dontwarn com.jfinal.template.Engine
-dontwarn com.jfinal.template.source.FileSourceFactory
-dontwarn com.jfinal.template.source.ISourceFactory
-dontwarn com.mayabot.nlp.segment.Lexer
-dontwarn com.mayabot.nlp.segment.Lexers
-dontwarn freemarker.cache.ClassTemplateLoader
-dontwarn freemarker.cache.FileTemplateLoader
-dontwarn freemarker.cache.TemplateLoader
-dontwarn freemarker.template.Configuration
-dontwarn freemarker.template.Version
-dontwarn java.awt.Color
-dontwarn java.awt.Font
-dontwarn java.awt.Point
-dontwarn java.awt.Rectangle
-dontwarn javax.money.CurrencyUnit
-dontwarn javax.money.Monetary
-dontwarn javax.ws.rs.Consumes
-dontwarn javax.ws.rs.Produces
-dontwarn javax.ws.rs.core.Response
-dontwarn javax.ws.rs.core.StreamingOutput
-dontwarn javax.ws.rs.ext.MessageBodyReader
-dontwarn javax.ws.rs.ext.MessageBodyWriter
-dontwarn javax.ws.rs.ext.Provider
-dontwarn net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
-dontwarn net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
-dontwarn net.sourceforge.pinyin4j.format.HanyuPinyinToneType
-dontwarn net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
-dontwarn org.ansj.splitWord.Analysis
-dontwarn org.ansj.splitWord.analysis.ToAnalysis
-dontwarn org.apache.commons.logging.impl.Log4JLogger
-dontwarn org.apache.log4j.Logger
-dontwarn org.apache.logging.log4j.LogManager
-dontwarn org.apache.logging.log4j.Logger
-dontwarn org.apache.lucene.analysis.Analyzer
-dontwarn org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
-dontwarn org.apache.velocity.app.VelocityEngine
-dontwarn org.apache.velocity.runtime.resource.loader.ResourceLoader
-dontwarn org.apdplat.word.segmentation.Segmentation
-dontwarn org.apdplat.word.segmentation.SegmentationAlgorithm
-dontwarn org.apdplat.word.segmentation.SegmentationFactory
-dontwarn org.beetl.core.Configuration
-dontwarn org.beetl.core.GroupTemplate
-dontwarn org.beetl.core.ResourceLoader
-dontwarn org.beetl.core.resource.ClasspathResourceLoader
-dontwarn org.beetl.core.resource.CompositeResourceLoader
-dontwarn org.beetl.core.resource.FileResourceLoader
-dontwarn org.beetl.core.resource.StringTemplateResourceLoader
-dontwarn org.beetl.core.resource.WebAppResourceLoader
-dontwarn org.glassfish.jersey.internal.spi.AutoDiscoverable
-dontwarn org.javamoney.moneta.Money
-dontwarn org.jboss.logging.Logger
-dontwarn org.joda.time.DateTime
-dontwarn org.joda.time.DateTimeZone
-dontwarn org.joda.time.Duration
-dontwarn org.joda.time.Instant
-dontwarn org.joda.time.LocalDate
-dontwarn org.joda.time.LocalDateTime
-dontwarn org.joda.time.LocalTime
-dontwarn org.joda.time.Period
-dontwarn org.joda.time.ReadablePartial
-dontwarn org.joda.time.format.DateTimeFormat
-dontwarn org.joda.time.format.DateTimeFormatter
-dontwarn org.lionsoul.jcseg.ISegment$Type
-dontwarn org.lionsoul.jcseg.ISegment
-dontwarn org.lionsoul.jcseg.dic.ADictionary
-dontwarn org.lionsoul.jcseg.dic.DictionaryFactory
-dontwarn org.lionsoul.jcseg.fi.SegmenterFunction
-dontwarn org.lionsoul.jcseg.segmenter.SegmenterConfig
-dontwarn org.pmw.tinylog.Level
-dontwarn org.pmw.tinylog.Logger
-dontwarn org.rythmengine.RythmEngine
-dontwarn org.slf4j.ILoggerFactory
-dontwarn org.slf4j.Logger
-dontwarn org.slf4j.LoggerFactory
-dontwarn org.slf4j.helpers.NOPLoggerFactory
-dontwarn org.slf4j.spi.LocationAwareLogger
-dontwarn org.thymeleaf.TemplateEngine
-dontwarn org.thymeleaf.templatemode.TemplateMode
-dontwarn org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
-dontwarn org.thymeleaf.templateresolver.DefaultTemplateResolver
-dontwarn org.thymeleaf.templateresolver.FileTemplateResolver
-dontwarn org.thymeleaf.templateresolver.ITemplateResolver
-dontwarn org.thymeleaf.templateresolver.StringTemplateResolver
-dontwarn org.tinylog.Level
-dontwarn org.tinylog.Logger
-dontwarn org.tinylog.configuration.Configuration
-dontwarn org.tinylog.format.AdvancedMessageFormatter
-dontwarn org.tinylog.format.MessageFormatter
-dontwarn org.tinylog.provider.LoggingProvider
-dontwarn org.tinylog.provider.ProviderRegistry
-dontwarn org.wltea.analyzer.core.IKSegmenter
-dontwarn springfox.documentation.spring.web.json.Json