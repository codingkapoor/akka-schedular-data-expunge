package com.codingkapoor.dataexpunge.core

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import akka.actor.{Cancellable, FSM, Props}
import akka.util.Timeout
import com.codingkapoor.dataexpunge.`package`.getCustomerDbInfo
import com.codingkapoor.dataexpunge.{Do, Done}
import com.codingkapoor.dataexpunge.core.Scheduler.{Data, State}

import scala.concurrent.duration._

object Scheduler {

  sealed trait State

  case object Init extends State

  case object Active extends State

  sealed trait Data

  case object Uninitialized extends Data

  case class StateData(deConfigsPerDb: DeConfigsPerDb, lastDo: LocalDateTime, lastDuration: Int, nextDo: LocalDateTime, isRescheduleDue: Boolean = false) extends Data

  case class NewDeConfigsPerDb(deConfigsPerDb: DeConfigsPerDb)

  def props: Props = Props[Scheduler]

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))
}

class Scheduler extends FSM[State, Data] with Logger {

  import Scheduler._
  import context.dispatcher

  lazy val actorName: String = this.self.path.name

  logger.info(s"Created $actorName.")

  lazy val (db, customer) = getCustomerDbInfo(actorName)

  var akkaScheduler: Option[Cancellable] = None
  var dataExpunge: DataExpungeActorRef = _

  def schedule(initialDelay: Int, interval: Int, ttl: Ttl): Unit = {
    val _akkaScheduler = context.system.scheduler.schedule(initialDelay second, interval seconds, dataExpunge, Do(ttl))
    akkaScheduler = Some(_akkaScheduler)
  }

  def reschedule(initialDelay: Int, interval: Int, ttl: Ttl): Unit = {
    if (akkaScheduler.isEmpty)
      schedule(initialDelay, interval, ttl)
    else {
      if (akkaScheduler.get.cancel())
        schedule(initialDelay, interval, ttl)
    }
  }

  startWith(Init, Uninitialized)

  when(Init) {
    case Event((NewDeConfigsPerDb(deConfigsPerDb), dataExpunge: DataExpungeActorRef), Uninitialized) =>
      this.dataExpunge = dataExpunge

      logger.debug(s"""customer = $customer, db = $db, event = $NewDeConfigsPerDb($deConfigsPerDb), state = $Init, stateData = $Uninitialized""")

      dataExpunge ! Do(deConfigsPerDb.ttl)

      val current = LocalDateTime.now
      goto(Active) using StateData(deConfigsPerDb, lastDo = current, lastDuration = 0, nextDo = current, isRescheduleDue = true)
  }

  when(Active) {
    case Event(NewDeConfigsPerDb(deConfigsPerDb), stateData: StateData) =>

      logger.debug(s"""customer = $customer, db = $db, event = $NewDeConfigsPerDb($deConfigsPerDb), state = $Active, stateData = $stateData""")

      val freq = deConfigsPerDb.freq
      val ttl = deConfigsPerDb.ttl

      val current = LocalDateTime.now
      var nextDo = stateData.nextDo
      var isRescheduleDue = false

      // These validations are required since lastDo is updated only after Done event is received.
      if (current isBefore stateData.nextDo) {
        val spent = java.time.Duration.between(stateData.lastDo, current).getSeconds.toInt
        val initialDelay = if (freq - spent >= 0) freq - spent else 0
        nextDo = current plusSeconds initialDelay

        logger.debug(s"""customer = $customer, db = $db, event = $NewDeConfigsPerDb($deConfigsPerDb), state = $Active, current = $current, duration = $spent, initialDelay = $initialDelay, nextDo = $nextDo""")

        reschedule(initialDelay, freq, ttl)

      } else if ((current isAfter stateData.nextDo) || (current isEqual stateData.nextDo)) {
        // If ttls are modified while scheduled task is already under execution then delay rescheduling up until Done event is received from that task
        isRescheduleDue = true
      }

      stay using StateData(deConfigsPerDb, lastDo = stateData.lastDo, lastDuration = 0, nextDo, isRescheduleDue)

    case Event(Done, stateData: StateData) =>

      logger.debug(s"""customer = $customer, db = $db, event = $Done, state = $Active, stateData = $stateData""")

      val freq = stateData.deConfigsPerDb.freq
      val ttl = stateData.deConfigsPerDb.ttl

      val _lastDo = stateData.nextDo

      // val duration = LocalDate.now.toEpochDay - stateData.last.toEpochDay
      val doneIn = LocalDateTime.now
      val currentDuration = java.time.Duration.between(_lastDo, doneIn).getSeconds.toInt
      val durationDelta = if (currentDuration != 0) currentDuration - stateData.lastDuration else currentDuration

      logger.debug(s"customer = $customer, db = $db, event = $Done, state = $Active, doneIn = $doneIn, currentDuration = $currentDuration, lastDuration = ${stateData.lastDuration}, durationDelta = $durationDelta")

      var newFreq = freq
      var nextDo = _lastDo plusSeconds freq

      if (durationDelta != 0 || stateData.isRescheduleDue) {
        val initialDelay = newFreq
        val interval = newFreq + durationDelta

        newFreq = interval
        nextDo = _lastDo plusSeconds interval

        reschedule(initialDelay, interval, ttl)
      }

      logger.debug(s"customer = $customer, db = $db, event = $Done, state = $Active, freq = $newFreq, lastDo = ${_lastDo}, lastDuration = $currentDuration, nextDo = $nextDo")

      stay using StateData(DeConfigsPerDb(ttl, newFreq), lastDo = _lastDo, lastDuration = currentDuration, nextDo)
  }

  whenUnhandled {
    case Event(e, s) =>
      logger.warn("event = {}, state = {}, Received unhandled request {}.", e, s, stateName)
      stay
  }

  initialize()
}
