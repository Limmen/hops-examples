package io.hops.examples.featurestore

import io.hops.examples.featurestore.featuregroups.ComputeFeatures
import org.apache.log4j.{ Level, LogManager, Logger }
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ Row, SparkSession }
import org.apache.spark.{ SparkConf, SparkContext }
import org.rogach.scallop.ScallopConf

/**
 * Parser of command-line arguments
 */
class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val input = opt[String](required = false, descr = "path to demo dataset folder")
  val jobid = opt[Int](required = false, validate = (0<), default = None, descr = "jobid for featuregroups")
  verify()
}

/**
 * Program entrypoint
 *
 * Sample Feature Engineering Job for the Hopsworks Feature
 */
object Main {

  def main(args: Array[String]): Unit = {

    // Setup logging
    val log = LogManager.getRootLogger()
    log.setLevel(Level.INFO)
    log.info(s"Starting Sample Feature Engineering Job For Feature Store Examples")

    //Parse cmd arguments
    val conf = new Conf(args)

    // Setup Spark
    var sparkConf: SparkConf = null
    sparkConf = sparkClusterSetup()

    val spark = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate();

    val sc = spark.sparkContext

    val clusterStr = sc.getConf.toDebugString
    log.info(s"Cluster settings: \n" + clusterStr)

    ComputeFeatures.computeGamesFeatureGroup(spark, log, conf.input(), conf.jobid())
    ComputeFeatures.computeSeasonScoresFeatureGroup(spark, log, conf.input(), conf.jobid())
    ComputeFeatures.computeAttendanceFeatureGroup(spark, log, conf.input(), conf.jobid())
    ComputeFeatures.computePlayersFeatureGroup(spark, log, conf.input(), conf.jobid())
    ComputeFeatures.computeTeamsFeatureGroup(spark, log, conf.input(), conf.jobid())

    import spark.implicits._

    log.info("Shutting down spark job")
    spark.close
  }

  /**
   * Hard coded settings for local spark training
   *
   * @return spark configurationh
   */
  def localSparkSetup(): SparkConf = {
    new SparkConf().setAppName("feature_engineering_spark").setMaster("local[*]")
  }

  /**
   * Hard coded settings for cluster spark training
   *
   * @return spark configuration
   */
  def sparkClusterSetup(): SparkConf = {
    new SparkConf().setAppName("feature_engineering_spark").set("spark.executor.heartbeatInterval", "20s").set("spark.rpc.message.maxSize", "512").set("spark.kryoserializer.buffer.max", "1024")
  }

}
