import com.webcommander.manager.PathManager
import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter
import org.yaml.snakeyaml.Yaml

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
if(Environment.isDevelopmentMode()) {
    appender('STDOUT', ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            charset = Charset.forName('UTF-8')

            pattern =
                    '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                            '%clr(%5p) ' + // Log level
                            '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                            '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                            '%m%n%wex' // Message
        }
    }
} else {
    appender("STDOUT", RollingFileAppender) {
        file = "${PathManager.getRoot()}logs/commander.log"
        rollingPolicy(FixedWindowRollingPolicy) {
            fileNamePattern = "${PathManager.getRoot()}logs/%i-commander.log"
            minIndex = 1
            maxIndex = 1000
        }
        triggeringPolicy(SizeBasedTriggeringPolicy) {
            maxFileSize = "2MB"
        }
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%date: [%level] [%logger] - %msg%n"
        }
    }
}

File customConfigFile = new File("grails-app/conf/config/application.yml")
def parsed = new Yaml().load(customConfigFile.exists() ? customConfigFile.text : "")
if(parsed?.logback?.verbose) {
    root(TRACE, ['STDOUT'])
} else {
    root(ERROR, ['STDOUT'])
}

if(parsed?.logback?.sql_log_params) {
    logger 'org.hibernate.type.descriptor.sql.BasicBinder', TRACE, ['STDOUT']
}

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir != null) {
    appender("FULL_STACKTRACE", RollingFileAppender) {
        file = "${targetDir}/stacktrace.log"
        rollingPolicy(FixedWindowRollingPolicy) {
            fileNamePattern = "${targetDir}/%i-stacktrace.log"
            minIndex = 1
            maxIndex = 1000
        }
        triggeringPolicy(SizeBasedTriggeringPolicy) {
            maxFileSize = "2MB"
        }
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%date: [%level] [%logger] - %msg%n"
        }
    }
    root(ERROR, ['STDOUT', 'FULL_STACKTRACE'])
} else {
    root(ERROR, ['STDOUT'])
}